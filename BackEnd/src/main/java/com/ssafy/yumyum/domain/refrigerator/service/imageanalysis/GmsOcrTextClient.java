package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.yumyum.domain.refrigerator.entity.FoodCategory;
import com.ssafy.yumyum.domain.refrigerator.entity.StorageLocation;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmsOcrTextClient {
    private static final String FAST_SYSTEM_PROMPT = """
            Convert noisy OCR lines from grocery photos or receipts into strict JSON for a refrigerator inventory app.
            Return JSON only. No markdown. Use only the provided OCR lines. If a value is unclear or guessed, return null.

            Input rules:
            - The OCR may be a product package or a receipt.
            - If the OCR is a receipt, choose the single clearest grocery item suitable for refrigerator inventory autofill.
            - Prefer fresh or refrigerator-relevant food over snacks or non-food items.
            - If no clear grocery item exists, set detected=false and explain briefly in message.

            Field rules:
            - name: exact product name from OCR text. Do not summarize or translate.
            - category: choose only when obvious.
            - quantity: only explicit amounts such as 500 g, 1 kg, 200 ml, 2 L, 3\uAC1C.
            - unit: one of \uAC1C, g, kg, ml, L.
            - expirationDate: only a full unambiguous date. Convert to YYYY-MM-DD.
            - storageLocation: classify carefully.
              REFRIGERATOR when the text says \uB0C9\uC7A5 or \uB0C9\uC7A5\uBCF4\uAD00, or when the product is clearly refrigerated such as milk, yogurt, cheese, tofu, fresh meat, packed salad, fresh noodles, or cut fruit.
              FREEZER when the text says \uB0C9\uB3D9 or \uB0C9\uB3D9\uBCF4\uAD00, or when the product is clearly frozen such as ice cream, frozen dumplings, frozen pizza, frozen meat, or frozen seafood.
              ROOM_TEMPERATURE when the text says \uC2E4\uC628, \uC0C1\uC628, \uC2E4\uC628\uBCF4\uAD00, or \uC0C1\uC628\uBCF4\uAD00, or when the product is clearly shelf-stable such as canned food, ramen, snacks, cereal, unopened bottled drinks, sauce, or seasoning.
              If signals conflict or stay ambiguous, return null.
            - memo: short Korean note with only reliable facts visible in the OCR text.
            - rawText: short OCR snippet most relevant to the extracted item.
            - detected: true only when a meaningful grocery item can actually be identified from OCR text.
            - confidence: number between 0 and 1.
            - message: short Korean message. If little can be extracted, clearly say so.
            """;
    private static final String REASONING_EFFORT = "minimal";
    private static final String VERBOSITY = "low";
    private static final int MAX_COMPLETION_TOKENS = 500;
    private static final int MAX_PROMPT_LINES = 20;
    private static final int MAX_PROMPT_CHARS = 1200;
    private static final int MAX_LINE_LENGTH = 140;

    private static final String SYSTEM_PROMPT = """
            You convert noisy OCR text from grocery photos into structured JSON for a refrigerator inventory app.
            Return strict JSON only. Do not return markdown.
            Use only the OCR text that is provided. Never infer from the image because you cannot see the image.
            If a field is unclear, partially visible, guessed from common sense, or not explicitly supported by the OCR text, return null.
            Be conservative. Wrong guesses are worse than null.
            Never invent brand names, product names, dates, quantities, categories, or storage methods.

            Input rules:
            - The OCR text may come from either a product package photo or a receipt photo.
            - OCR text may contain mistakes, duplicated fragments, spacing issues, or irrelevant text.
            - If the OCR looks like a receipt, choose one likely grocery item suitable for refrigerator inventory autofill.
            - If the receipt contains multiple items, choose the single clearest food item line.
            - Prefer refrigerated or fresh-food items over snacks or non-food items when several items are present.
            - If no clear food item can be identified, set detected=false and explain briefly in message.

            Field rules:
            - name: use the clearest exact product name from OCR text. Do not summarize or translate it.
            - category: choose only when the product identity is obvious.
              DAIRY = milk, cheese, yogurt, butter, cream
              MEAT = beef, pork, chicken, ham, sausage, bacon
              VEGETABLE = vegetables, salad, mushrooms, kimchi
              FRUIT = fruits, cut fruit, fruit packs
              ETC = clear grocery item but not in the categories above
              null = category is not clear
            - quantity: extract only explicit amount like 500 g, 1 kg, 200 ml, 2 L, 3개
            - unit: one of 개, g, kg, ml, L
            - expirationDate: return only if a full explicit date is present and unambiguous. Convert to YYYY-MM-DD.
            - storageLocation: return only when OCR text explicitly indicates 냉장, 냉동, 실온, or the item is unmistakably frozen.
              Use REFRIGERATOR, FREEZER, ROOM_TEMPERATURE only.
            - memo: short Korean note with only reliable extra facts visible in the OCR text, such as brand, 보관 방법, 소비기한 문구, or receipt item context.
            - rawText: short OCR snippet most relevant to the extracted item.
            - detected: true only when a meaningful grocery item can actually be identified from OCR text.
            - confidence: number between 0 and 1 reflecting extraction confidence.
            - message: short Korean message. If little can be extracted, clearly say so.

            JSON schema:
            {
              "detected": true,
              "name": "string or null",
              "category": "DAIRY|MEAT|VEGETABLE|FRUIT|ETC|null",
              "quantity": "number or null",
              "unit": "개|g|kg|ml|L|null",
              "expirationDate": "YYYY-MM-DD or null",
              "storageLocation": "REFRIGERATOR|FREEZER|ROOM_TEMPERATURE|null",
              "memo": "string or null",
              "rawText": "string or null",
              "confidence": 0.0,
              "message": "string"
            }
            """;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GmsOcrProperties properties;

    public OcrResult analyze(PororoOcrClient.PororoOcrText ocrText, String originalFilename) {
        if (!properties.isConfigured()) {
            throw new BusinessException(ExceptionType.OCR_API_KEY_NOT_CONFIGURED);
        }

        if (ocrText == null || !ocrText.detected() || ocrText.text() == null || ocrText.text().isBlank()) {
            return emptyResult("이미지에서 읽을 수 있는 텍스트를 찾지 못했습니다.");
        }

        PreparedPrompt prompt = buildUserPrompt(originalFilename, ocrText);
        log.info(
                "Structuring OCR text with model={}, rawChars={}, rawLines={}, promptChars={}, promptLines={}",
                properties.model(),
                prompt.rawChars(),
                prompt.rawLines(),
                prompt.promptChars(),
                prompt.promptLines()
        );

        Map<String, Object> payload = Map.of(
                "model", properties.model(),
                "response_format", Map.of("type", "json_object"),
                "reasoning_effort", REASONING_EFFORT,
                "verbosity", VERBOSITY,
                "max_completion_tokens", MAX_COMPLETION_TOKENS,
                "messages", List.of(
                        Map.of("role", "developer", "content", FAST_SYSTEM_PROMPT),
                        Map.of("role", "user", "content", prompt.content())
                )
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.apiKey());

            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.url(),
                    new HttpEntity<>(payload, headers),
                    String.class
            );
            return parseCompletionResponse(response.getBody());
        } catch (RestClientException exception) {
            log.error("Failed to structure OCR text", exception);
            throw new BusinessException(ExceptionType.OCR_ANALYSIS_FAILED);
        }
    }

    private OcrResult parseCompletionResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new IllegalArgumentException("Missing OCR content");
            }

            JsonNode json = objectMapper.readTree(content);
            String name = textOrNull(json, "name");
            String memo = textOrNull(json, "memo");
            String rawText = textOrNull(json, "rawText");
            OcrResult result = new OcrResult(
                    json.path("detected").asBoolean(false),
                    name,
                    parseEnum(textOrNull(json, "category"), FoodCategory.class),
                    decimalOrNull(json, "quantity"),
                    normalizeUnit(textOrNull(json, "unit")),
                    parseDate(textOrNull(json, "expirationDate")),
                    resolveStorageLocation(textOrNull(json, "storageLocation"), name, memo, rawText),
                    memo,
                    rawText,
                    doubleOrNull(json, "confidence"),
                    defaultMessage(textOrNull(json, "message"))
            );
            log.info("OCR structured result detected={}, name={}, rawText={}",
                    result.detected(), result.name(), result.rawText());
            return result;
        } catch (Exception exception) {
            log.error("Failed to parse structured OCR response: {}", responseBody, exception);
            throw new BusinessException(ExceptionType.OCR_ANALYSIS_FAILED);
        }
    }

    private PreparedPrompt buildUserPrompt(String originalFilename, PororoOcrClient.PororoOcrText ocrText) {
        List<String> sourceLines = sourceLines(ocrText);
        List<String> promptLines = preparePromptLines(sourceLines, ocrText.text());
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "filename", originalFilename == null || originalFilename.isBlank() ? "unknown" : originalFilename,
                    "lines", promptLines
            ));

            String content = """
                    Convert these OCR lines into the target JSON schema.
                    Use only the provided lines.
                    If you are not sure, use null.
                    OCR payload:
                    %s
                    """.formatted(payload);
            return new PreparedPrompt(
                    content,
                    ocrText.text().length(),
                    sourceLines.size(),
                    content.length(),
                    promptLines.size()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize OCR payload", exception);
        }
    }

    private List<String> preparePromptLines(List<String> sourceLines, String fallbackText) {
        ArrayList<String> prepared = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        int remainingChars = MAX_PROMPT_CHARS;

        for (String line : sourceLines) {
            if (prepared.size() >= MAX_PROMPT_LINES || remainingChars <= 0) {
                break;
            }

            String normalized = normalizePromptLine(line);
            if (normalized == null || !seen.add(normalized)) {
                continue;
            }

            if (normalized.length() > remainingChars) {
                if (remainingChars <= 3) {
                    break;
                }
                normalized = normalized.substring(0, remainingChars - 3).trim() + "...";
            }

            prepared.add(normalized);
            remainingChars -= normalized.length();
        }

        if (prepared.isEmpty() && fallbackText != null && !fallbackText.isBlank()) {
            String normalized = normalizePromptLine(fallbackText);
            if (normalized != null) {
                prepared.add(normalized.substring(0, Math.min(normalized.length(), MAX_PROMPT_CHARS)));
            }
        }

        return List.copyOf(prepared);
    }

    private List<String> sourceLines(PororoOcrClient.PororoOcrText ocrText) {
        if (ocrText.lines() != null && !ocrText.lines().isEmpty()) {
            return ocrText.lines();
        }
        if (ocrText.text() == null || ocrText.text().isBlank()) {
            return List.of();
        }
        return ocrText.text().lines().toList();
    }

    private String normalizePromptLine(String line) {
        if (line == null) {
            return null;
        }
        String normalized = line.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > MAX_LINE_LENGTH) {
            return normalized.substring(0, MAX_LINE_LENGTH - 3).trim() + "...";
        }
        return normalized;
    }

    private String textOrNull(JsonNode json, String fieldName) {
        JsonNode node = json.path(fieldName);
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String text = node.asText().trim();
        return text.isEmpty() || "null".equalsIgnoreCase(text) ? null : text;
    }

    private BigDecimal decimalOrNull(JsonNode json, String fieldName) {
        JsonNode node = json.path(fieldName);
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        String text = node.asText("").trim();
        if (text.isEmpty()) {
            return null;
        }
        return new BigDecimal(text);
    }

    private Double doubleOrNull(JsonNode json, String fieldName) {
        JsonNode node = json.path(fieldName);
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        return node.isNumber() ? node.doubleValue() : Double.parseDouble(node.asText());
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private String normalizeUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            return null;
        }
        return switch (unit.trim()) {
            case "개", "g", "kg", "ml", "L" -> unit.trim();
            case "l" -> "L";
            default -> null;
        };
    }

    private StorageLocation resolveStorageLocation(String storageLocation, String... evidence) {
        StorageLocation parsed = parseEnum(storageLocation, StorageLocation.class);
        return parsed != null ? parsed : inferStorageLocation(evidence);
    }

    private StorageLocation inferStorageLocation(String... evidence) {
        String source = String.join(" ", evidence == null ? new String[0] : evidence)
                .toLowerCase(Locale.ROOT);
        if (source.isBlank()) {
            return null;
        }
        if (containsAny(source, "\uB0C9\uB3D9", "\uB0C9\uB3D9\uBCF4\uAD00", "frozen", "\uC601\uD558")) {
            return StorageLocation.FREEZER;
        }
        if (containsAny(source, "\uB0C9\uC7A5", "\uB0C9\uC7A5\uBCF4\uAD00", "chilled")) {
            return StorageLocation.REFRIGERATOR;
        }
        if (containsAny(source, "\uC2E4\uC628", "\uC0C1\uC628", "\uC2E4\uC628\uBCF4\uAD00", "\uC0C1\uC628\uBCF4\uAD00", "room temperature")) {
            return StorageLocation.ROOM_TEMPERATURE;
        }
        return null;
    }

    private boolean containsAny(String source, String... values) {
        for (String value : values) {
            if (source.contains(value.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private <T extends Enum<T>> T parseEnum(String value, Class<T> enumClass) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String defaultMessage(String message) {
        return message == null || message.isBlank()
                ? "OCR 텍스트를 바탕으로 자동 입력 정보를 정리했습니다."
                : message;
    }

    private OcrResult emptyResult(String message) {
        return new OcrResult(
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0.0,
                message
        );
    }

    public record OcrResult(
            boolean detected,
            String name,
            FoodCategory category,
            BigDecimal quantity,
            String unit,
            LocalDate expirationDate,
            StorageLocation storageLocation,
            String memo,
            String rawText,
            Double confidence,
            String message
    ) {
    }

    private record PreparedPrompt(
            String content,
            int rawChars,
            int rawLines,
            int promptChars,
            int promptLines
    ) {
    }
}
