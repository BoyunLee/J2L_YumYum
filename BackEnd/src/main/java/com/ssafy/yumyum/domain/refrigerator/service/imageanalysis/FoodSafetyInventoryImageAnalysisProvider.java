package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.ssafy.yumyum.domain.refrigerator.dto.ImageAnalysisType;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryImageAnalysisBatchResponse;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryImageAnalysisItemResponse;
import com.ssafy.yumyum.domain.refrigerator.entity.FoodCategory;
import com.ssafy.yumyum.domain.refrigerator.entity.StorageLocation;
import com.ssafy.yumyum.global.config.IntegrationModeProperties;
import com.ssafy.yumyum.global.exception.BusinessException;

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
    private final IntegrationModeProperties integrationModeProperties;

    @Override
    public InventoryImageAnalysisBatchResponse analyze(
            ImageAnalysisType analysisType,
            byte[] imageBytes,
            String contentType,
            String originalFilename) {
        if (integrationModeProperties.isMock()) {
            return mockAnalysis(analysisType);
        }
        if (integrationModeProperties.isFallback()) {
            try {
                return analyzeLive(analysisType, imageBytes, contentType, originalFilename);
            } catch (BusinessException exception) {
                log.warn("Inventory image analysis failed in fallback mode: code={}",
                        exception.getExceptionType().getCode());
                return mockAnalysis(analysisType);
            }
        }
        return analyzeLive(analysisType, imageBytes, contentType, originalFilename);
    }

    private InventoryImageAnalysisBatchResponse analyzeLive(
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

    private InventoryImageAnalysisBatchResponse mockAnalysis(ImageAnalysisType analysisType) {
        if (analysisType == ImageAnalysisType.BARCODE) {
            return new InventoryImageAnalysisBatchResponse(
                    ImageAnalysisType.BARCODE,
                    true,
                    List.of(new InventoryImageAnalysisItemResponse(
                            "데모 우유 900ml",
                            FoodCategory.DAIRY,
                            BigDecimal.valueOf(900),
                            "ml",
                            LocalDate.now().plusDays(7),
                            StorageLocation.REFRIGERATOR,
                            "mock 모드에서 제공되는 바코드 데모 데이터입니다.",
                            "8800000000000",
                            null,
                            1.0
                    )),
                    "mock 모드에서 바코드 데모 데이터를 불러왔습니다."
            );
        }

        return new InventoryImageAnalysisBatchResponse(
                ImageAnalysisType.OCR,
                true,
                List.of(
                        new InventoryImageAnalysisItemResponse(
                                "데모 계란",
                                FoodCategory.ETC,
                                BigDecimal.valueOf(10),
                                "개",
                                LocalDate.now().plusDays(14),
                                StorageLocation.REFRIGERATOR,
                                "mock 모드에서 제공되는 OCR 데모 데이터입니다.",
                                null,
                                "데모 계란 10개 냉장 보관",
                                0.99
                        ),
                        new InventoryImageAnalysisItemResponse(
                                "데모 샐러드",
                                FoodCategory.VEGETABLE,
                                BigDecimal.ONE,
                                "개",
                                LocalDate.now().plusDays(3),
                                StorageLocation.REFRIGERATOR,
                                "mock 모드에서 제공되는 OCR 데모 데이터입니다.",
                                null,
                                "데모 샐러드 1개 소비기한 임박",
                                0.95
                        )
                ),
                "mock 모드에서 OCR 데모 데이터를 불러왔습니다."
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
