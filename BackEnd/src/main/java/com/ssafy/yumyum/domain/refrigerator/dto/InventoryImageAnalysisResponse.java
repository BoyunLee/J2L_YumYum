package com.ssafy.yumyum.domain.refrigerator.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ssafy.yumyum.domain.refrigerator.entity.FoodCategory;
import com.ssafy.yumyum.domain.refrigerator.entity.StorageLocation;

/**
 * 이미지 분석 업체와 프론트엔드 사이의 공통 응답 계약입니다.
 * 인식하지 못한 필드는 null로 반환하고, enum/단위 값은 수동 등록 API와 같은 값으로 정규화합니다.
 */
public record InventoryImageAnalysisResponse(
        ImageAnalysisType analysisType,
        boolean detected,
        String name,
        FoodCategory category,
        BigDecimal quantity,
        String unit,
        LocalDate expirationDate,
        StorageLocation storageLocation,
        String memo,
        String barcode,
        String rawText,
        Double confidence,
        String message
) {
    public static InventoryImageAnalysisResponse pendingProvider(ImageAnalysisType analysisType) {
        return new InventoryImageAnalysisResponse(
                analysisType,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "이미지 업로드를 확인했습니다. 분석 API provider를 연결하면 인식 결과가 자동 입력됩니다."
        );
    }
}
