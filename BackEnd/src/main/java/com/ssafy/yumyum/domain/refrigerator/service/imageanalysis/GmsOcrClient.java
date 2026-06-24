package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import static java.util.Map.entry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
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
import com.ssafy.yumyum.domain.refrigerator.entity.FoodCategory;
import com.ssafy.yumyum.domain.refrigerator.entity.StorageLocation;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmsOcrClient {
    private static final String SYSTEM_PROMPT = """
            You are an OCR extraction model for a Korean refrigerator inventory app.
            Your job is to extract only clearly visible product information from either:
            1. a grocery product package photo, or
            2. a grocery receipt photo.
            Return strict JSON only. Do not return markdown.
            If a field is unclear, partially visible, guessed from common sense, or not explicitly shown, return null.
            Be conservative. Wrong guesses are worse than null.
            Never invent brand names, product names, dates, quantities, categories, or storage methods.
            Ignore decorative phrases, marketing copy, event text, background objects, hands, tables, receipts, and unrelated products.

            Input type rules:
            - If the image is a product package, extract from the package.
            - If the image is a receipt, detect that it is a receipt and extract one likely grocery item suitable for refrigerator inventory autofill.
            - If the receipt contains multiple items, choose the single clearest food item line.
            - Prefer refrigerated or fresh-food items over snacks or non-food items when several items are present.
            - If no clear food item can be identified from the receipt, set detected=false and explain briefly in message.

            Field rules:
            - name: use the main product name printed on the package. Prefer the most prominent exact label text. Do not summarize or translate it.
              If the image is a receipt, use the exact item name from one receipt line, cleaned only minimally.
            - category: choose only when the package text or product identity is obvious.
              DAIRY = milk, cheese, yogurt, butter, cream
              MEAT = beef, pork, chicken, ham, sausage, bacon
              VEGETABLE = vegetables, salad, mushrooms, kimchi
              FRUIT = fruits, cut fruit, fruit packs
              ETC = clear grocery item but not in the categories above
              null = category is not clear
            - quantity: extract only visible package amount like 500 g, 1 kg, 200 ml, 2 L, 3개.
              For receipts, use quantity only if the receipt line explicitly shows a count or amount. Do not infer from price.
            - unit: one of 개, g, kg, ml, L
            - expirationDate: return only if a full explicit date is visible and unambiguous. Convert to YYYY-MM-DD. If only part of the date is visible, return null.
            - storageLocation: return only when the package explicitly indicates 냉장/냉동/실온 or the item is unmistakably frozen.
              Use REFRIGERATOR, FREEZER, ROOM_TEMPERATURE only.
              If not explicit, return null.
            - memo: short Korean note with only reliable extra facts visible in the image, such as brand, 보관방법 문구, 소비기한 표기 문구, or "영수증 인식" and the selected receipt line. No speculation.
            - rawText: short OCR text snippet of the most important visible package text, preserving original language as much as possible.
            - detected: true only when a meaningful product package or label is actually readable.
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
    private final OcrImageCompressor ocrImageCompressor;

    public OcrResult analyze(byte[] imageBytes, String contentType, String originalFilename) {
        if (!properties.isConfigured()) {
            throw new BusinessException(ExceptionType.OCR_API_KEY_NOT_CONFIGURED);
        }

        OcrImageCompressor.CompressedImage preparedImage = ocrImageCompressor.compress(imageBytes, contentType);
        String imageDataUrl = "data:" + preparedImage.contentType()
                + ";base64," + Base64.getEncoder().encodeToString(preparedImage.bytes());

        log.info(
                "OCR request model={}, bytes={} -> {}, size={}x{} -> {}x{}, compressed={}",
                properties.model(),
                imageBytes.length,
                preparedImage.bytes().length,
                preparedImage.originalWidth(),
                preparedImage.originalHeight(),
                preparedImage.width(),
                preparedImage.height(),
                preparedImage.compressed()
        );
        Map<String, Object> payload = Map.of(
                "model", properties.model(),
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "developer", "content", SYSTEM_PROMPT),
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.ofEntries(
                                                entry("type", "text"),
                                                entry("text", buildUserPrompt(originalFilename))
                                        ),
                                        Map.ofEntries(
                                                entry("type", "image_url"),
                                                entry("image_url", Map.of(
                                                        "url", imageDataUrl,
                                                        "detail", "high"
                                                ))
                                        )
                                )
                        )
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
            log.error("Failed to call OCR API", exception);
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
            OcrResult result = new OcrResult(
                    json.path("detected").asBoolean(false),
                    textOrNull(json, "name"),
                    parseEnum(textOrNull(json, "category"), FoodCategory.class),
                    decimalOrNull(json, "quantity"),
                    normalizeUnit(textOrNull(json, "unit")),
                    parseDate(textOrNull(json, "expirationDate")),
                    parseEnum(textOrNull(json, "storageLocation"), StorageLocation.class),
                    textOrNull(json, "memo"),
                    textOrNull(json, "rawText"),
                    doubleOrNull(json, "confidence"),
                    defaultMessage(textOrNull(json, "message"))
            );
            log.info("OCR parsed result detected={}, name={}, rawText={}",
                    result.detected(), result.name(), result.rawText());
            return result;
        } catch (Exception exception) {
            log.error("Failed to parse OCR API response: {}", responseBody, exception);
            throw new BusinessException(ExceptionType.OCR_ANALYSIS_FAILED);
        }
    }

    private String buildUserPrompt(String originalFilename) {
        return """
                Analyze this grocery image for refrigerator item autofill.
                The filename is: %s
                The image may be either a product package photo or a receipt photo.
                Extract only fields that are directly visible.
                If you are not sure, use null.
                Focus on:
                1. exact product name on package or receipt line
                2. visible amount and unit
                3. explicit expiration/best-before date
                4. explicit storage instruction
                5. short reliable OCR text
                If this is a receipt with multiple items, choose one clear food item only.
                Do not infer missing values from typical products.
                """.formatted(originalFilename == null || originalFilename.isBlank() ? "unknown" : originalFilename);
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

    private <T extends Enum<T>> T parseEnum(String value, Class<T> enumClass) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String defaultMessage(String message) {
        return message == null || message.isBlank()
                ? "이미지에서 읽은 정보를 바탕으로 자동 입력했습니다."
                : message;
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

}
