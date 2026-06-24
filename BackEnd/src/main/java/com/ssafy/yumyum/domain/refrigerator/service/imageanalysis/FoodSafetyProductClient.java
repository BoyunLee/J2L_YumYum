package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class FoodSafetyProductClient {
    private static final String API_KEY_PLACEHOLDER = "여기에_식품안전나라_API_인증키_입력";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${food-safety.api.base-url:http://openapi.foodsafetykorea.go.kr/api}")
    private String baseUrl;

    @Value("${food-safety.api.key:여기에_식품안전나라_API_인증키_입력}")
    private String apiKey;

    public FoodSafetyProduct findByBarcode(String barcode) {
        validateApiKey();
        try {
            String responseBody = restTemplate.getForObject(buildRequestUri(barcode), String.class);
            return parseResponse(responseBody, barcode);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException | JacksonException exception) {
            throw new BusinessException(ExceptionType.FOOD_SAFETY_API_ERROR);
        }
    }

    private URI buildRequestUri(String barcode) {
        String encodedKey = UriUtils.encodePathSegment(apiKey, StandardCharsets.UTF_8);
        String encodedBarcode = UriUtils.encodePathSegment(barcode, StandardCharsets.UTF_8);
        String url = baseUrl.replaceAll("/+$", "")
                + "/" + encodedKey
                + "/C005/json/1/1/BAR_CD=" + encodedBarcode;
        return URI.create(url);
    }

    private FoodSafetyProduct parseResponse(String responseBody, String barcode) throws JacksonException {
        if (responseBody == null || responseBody.isBlank()) {
            throw new BusinessException(ExceptionType.FOOD_SAFETY_API_ERROR);
        }

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode service = root.path("C005");
        JsonNode result = service.isMissingNode() ? root.path("RESULT") : service.path("RESULT");
        String resultCode = text(result, "CODE");

        if (!resultCode.isBlank() && !"INFO-000".equals(resultCode) && !"INFO-200".equals(resultCode)) {
            throw new BusinessException(ExceptionType.FOOD_SAFETY_API_ERROR);
        }

        JsonNode rows = service.path("row");
        if (!rows.isArray() || rows.isEmpty()) {
            throw new BusinessException(ExceptionType.BARCODE_PRODUCT_NOT_FOUND);
        }

        JsonNode row = rows.get(0);
        return new FoodSafetyProduct(
                defaultIfBlank(text(row, "BAR_CD"), barcode),
                text(row, "PRDLST_NM"),
                text(row, "POG_DAYCNT"),
                text(row, "PRDLST_DCNM"),
                text(row, "BSSH_NM"),
                text(row, "PRDLST_REPORT_NO")
        );
    }

    private String text(JsonNode node, String fieldName) {
        return node.path(fieldName).asString("").trim();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value.isBlank() ? defaultValue : value;
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.isBlank() || API_KEY_PLACEHOLDER.equals(apiKey)) {
            throw new BusinessException(ExceptionType.FOOD_SAFETY_API_KEY_NOT_CONFIGURED);
        }
    }
}
