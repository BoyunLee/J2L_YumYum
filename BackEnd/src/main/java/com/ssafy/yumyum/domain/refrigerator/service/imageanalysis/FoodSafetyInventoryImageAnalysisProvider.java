package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.ssafy.yumyum.domain.refrigerator.dto.ImageAnalysisType;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryImageAnalysisBatchResponse;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryImageAnalysisItemResponse;
import com.ssafy.yumyum.domain.refrigerator.entity.FoodCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FoodSafetyInventoryImageAnalysisProvider implements InventoryImageAnalysisProvider {
    private static final Pattern QUANTITY_PATTERN = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)\\s*(kg|g|ml|l|개)",
            Pattern.CASE_INSENSITIVE
    );

    private final BarcodeDecoder barcodeDecoder;
    private final FoodSafetyProductClient foodSafetyProductClient;
    private final PororoOcrClient pororoOcrClient;
    private final GmsOcrBatchTextClient gmsOcrTextClient;

    @Override
    public InventoryImageAnalysisBatchResponse analyze(
            ImageAnalysisType analysisType,
            byte[] imageBytes,
            String contentType,
            String originalFilename) {
        if (analysisType == ImageAnalysisType.OCR) {
            PororoOcrClient.PororoOcrText ocrText = pororoOcrClient.extractText(
                    imageBytes,
                    contentType,
                    originalFilename
            );
            GmsOcrBatchTextClient.OcrResult result = gmsOcrTextClient.analyze(ocrText, originalFilename);
            return new InventoryImageAnalysisBatchResponse(
                    ImageAnalysisType.OCR,
                    result.detected(),
                    result.items().stream()
                            .map(item -> new InventoryImageAnalysisItemResponse(
                                    item.name(),
                                    item.category(),
                                    item.quantity(),
                                    item.unit(),
                                    item.expirationDate(),
                                    item.storageLocation(),
                                    item.memo(),
                                    null,
                                    item.rawText(),
                                    item.confidence()
                            ))
                            .toList(),
                    result.message()
            );
        }

        String barcode = barcodeDecoder.decode(imageBytes);
        log.info("Decoded barcode: {}", barcode);
        FoodSafetyProduct product = foodSafetyProductClient.findByBarcode(barcode);
        Quantity quantity = parseQuantity(product.productName());

        return new InventoryImageAnalysisBatchResponse(
                ImageAnalysisType.BARCODE,
                true,
                List.of(new InventoryImageAnalysisItemResponse(
                        product.productName(),
                        mapCategory(product.productName(), product.foodType()),
                        quantity.value(),
                        quantity.unit(),
                        null,
                        null,
                        buildMemo(product),
                        product.barcode(),
                        null,
                        1.0
                )),
                "바코드와 식품안전나라 제품 정보를 확인했습니다."
        );
    }

    private Quantity parseQuantity(String productName) {
        Matcher matcher = QUANTITY_PATTERN.matcher(productName == null ? "" : productName);
        if (!matcher.find()) {
            return new Quantity(null, null);
        }
        String unit = matcher.group(2);
        unit = "l".equalsIgnoreCase(unit) ? "L" : unit.toLowerCase(Locale.ROOT);
        return new Quantity(new BigDecimal(matcher.group(1)), unit);
    }

    private FoodCategory mapCategory(String productName, String foodType) {
        String source = ((productName == null ? "" : productName) + " "
                + (foodType == null ? "" : foodType)).toLowerCase(Locale.ROOT);
        if (containsAny(source, "우유", "유제품", "치즈", "요구르트", "요거트", "발효유")) {
            return FoodCategory.DAIRY;
        }
        if (containsAny(source, "육류", "소고기", "쇠고기", "돼지고기", "닭고기", "햄", "소시지")) {
            return FoodCategory.MEAT;
        }
        if (containsAny(source, "채소", "야채", "김치", "나물")) {
            return FoodCategory.VEGETABLE;
        }
        if (containsAny(source, "과일", "과채", "사과", "배", "포도", "감귤")) {
            return FoodCategory.FRUIT;
        }
        return FoodCategory.ETC;
    }

    private boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String buildMemo(FoodSafetyProduct product) {
        StringBuilder memo = new StringBuilder();
        appendMemo(memo, "식품 유형", product.foodType());
        appendMemo(memo, "제조사", product.manufacturer());
        appendMemo(memo, "소비기한 기준", product.shelfLife());
        appendMemo(memo, "품목보고번호", product.productReportNumber());
        return memo.toString();
    }

    private void appendMemo(StringBuilder memo, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!memo.isEmpty()) {
            memo.append('\n');
        }
        memo.append(label).append(": ").append(value);
    }

    private record Quantity(BigDecimal value, String unit) {
    }
}
