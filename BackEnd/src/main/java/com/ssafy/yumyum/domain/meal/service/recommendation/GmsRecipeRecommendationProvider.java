package com.ssafy.yumyum.domain.meal.service.recommendation;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationItemRequest;
import com.ssafy.yumyum.domain.refrigerator.entity.RefrigeratorItem;
import com.ssafy.yumyum.domain.refrigerator.service.imageanalysis.GmsOcrProperties;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmsRecipeRecommendationProvider {
    private static final int MAX_INVENTORY_ITEMS = 30;
    private static final int MAX_COMPLETION_TOKENS = 900;
    private static final String SYSTEM_PROMPT = """
            당신은 냉장고 재료를 낭비하지 않도록 돕는 한국어 요리 전문가입니다.
            주어진 재료와 유통기한을 고려해 만들기 좋은 레시피를 2개 추천하세요.
            보유 재료를 우선 사용하고, 없는 재료는 최소화하세요.
            설명은 한 문장, 조리 단계는 레시피당 최대 4개, 팁은 1개만 작성하세요.
            응답은 마크다운 없이 반드시 {"recipes": [...]} 형태의 유효한 JSON만 반환하세요.
            각 레시피는 name, description, matchRate, cookTime, servings, difficulty, calories,
            availableIngredients, missingIngredients, steps, tips를 포함해야 합니다.
            """;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GmsOcrProperties properties;

    public List<RecipeRecommendationItemRequest> recommend(List<RefrigeratorItem> inventory) {
        if (!properties.isConfigured()) {
            throw new BusinessException(ExceptionType.RECIPE_RECOMMENDATION_API_KEY_NOT_CONFIGURED);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.apiKey());

            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.url(),
                    new HttpEntity<>(payload(inventory), headers),
                    String.class
            );
            return parseCompletionResponse(response.getBody());
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            log.warn("Failed to call recipe recommendation API: {}", exception.getMessage());
            throw new BusinessException(ExceptionType.RECIPE_RECOMMENDATION_FAILED);
        } catch (Exception exception) {
            log.warn("Failed to parse recipe recommendation API response", exception);
            throw new BusinessException(ExceptionType.RECIPE_RECOMMENDATION_FAILED);
        }
    }

    private Map<String, Object> payload(List<RefrigeratorItem> inventory) {
        List<Map<String, Object>> ingredients = inventory.stream()
                .limit(MAX_INVENTORY_ITEMS)
                .map(this::ingredientPayload)
                .toList();

        return Map.of(
                "model", properties.model(),
                "response_format", Map.of("type", "json_object"),
                "reasoning_effort", "minimal",
                "verbosity", "low",
                "max_completion_tokens", MAX_COMPLETION_TOKENS,
                "messages", List.of(
                        Map.of("role", "developer", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", "현재 냉장고 재료입니다: " + toJson(ingredients))
                )
        );
    }

    private Map<String, Object> ingredientPayload(RefrigeratorItem item) {
        return Map.of(
                "name", item.getName(),
                "quantity", item.getQuantity() == null ? BigDecimal.ONE : item.getQuantity(),
                "unit", item.getUnit() == null ? "개" : item.getUnit(),
                "expiryDate", item.getExpirationDate() == null
                        ? ""
                        : DateTimeFormatter.ISO_DATE.format(item.getExpirationDate())
        );
    }

    private List<RecipeRecommendationItemRequest> parseCompletionResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String content = root.path("choices").path(0).path("message").path("content").asText(null);
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Missing recommendation content");
        }

        JsonNode json = objectMapper.readTree(stripMarkdownFence(content));
        JsonNode recipesNode = json.path("recipes");
        if (!recipesNode.isArray() || recipesNode.isEmpty()) {
            throw new IllegalArgumentException("Missing recipes array");
        }

        List<RecipeRecommendationItemRequest> recipes = new ArrayList<>();
        for (JsonNode recipeNode : recipesNode) {
            recipes.add(toRecipe(recipeNode));
        }
        if (recipes.isEmpty()) {
            throw new BusinessException(ExceptionType.RECIPE_RECOMMENDATION_FAILED);
        }
        return List.copyOf(recipes);
    }

    private RecipeRecommendationItemRequest toRecipe(JsonNode node) {
        List<String> availableIngredients = strings(node.path("availableIngredients"));
        List<String> missingIngredients = strings(node.path("missingIngredients"));
        List<String> steps = strings(node.path("steps"));
        List<String> tips = strings(node.path("tips"));

        return new RecipeRecommendationItemRequest(
                truncate(defaultText(node, "name", "냉장고 추천 레시피"), 200),
                truncate(defaultText(node, "description", "냉장고 재료로 만드는 추천 요리입니다."), 1000),
                clamp(node.path("matchRate").asInt(0), 0, 100),
                Math.max(1, node.path("cookTime").asInt(1)),
                Math.max(1, node.path("servings").asInt(1)),
                truncate(defaultText(node, "difficulty", "보통"), 30),
                Math.max(0, node.path("calories").asInt(0)),
                availableIngredients.isEmpty() ? List.of("보유 재료") : availableIngredients,
                missingIngredients,
                steps.isEmpty() ? List.of("재료를 손질합니다.", "먹기 좋게 조리합니다.") : steps,
                tips
        );
    }

    private List<String> strings(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            String text = item.asText("").trim();
            if (!text.isBlank()) {
                values.add(truncate(text, 200));
            }
        }
        return List.copyOf(values);
    }

    private String defaultText(JsonNode node, String fieldName, String defaultValue) {
        String text = node.path(fieldName).asText("").trim();
        return text.isBlank() ? defaultValue : text;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String stripMarkdownFence(String content) {
        return content.trim()
                .replaceFirst("^```(?:json)?\\s*", "")
                .replaceFirst("\\s*```$", "");
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("레시피 추천 입력 데이터를 JSON으로 변환하지 못했습니다.", exception);
        }
    }
}
