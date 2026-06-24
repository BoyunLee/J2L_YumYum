package com.ssafy.yumyum.domain.refrigerator.dto;

import java.util.List;

public record InventoryImageAnalysisBatchResponse(
        ImageAnalysisType analysisType,
        boolean detected,
        List<InventoryImageAnalysisItemResponse> items,
        String message
) {
    public InventoryImageAnalysisBatchResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static InventoryImageAnalysisBatchResponse pendingProvider(ImageAnalysisType analysisType) {
        return new InventoryImageAnalysisBatchResponse(
                analysisType,
                false,
                List.of(),
                "이미지 업로드는 확인했습니다. 분석 API provider 를 연결하면 인식 결과가 자동 입력됩니다."
        );
    }
}
