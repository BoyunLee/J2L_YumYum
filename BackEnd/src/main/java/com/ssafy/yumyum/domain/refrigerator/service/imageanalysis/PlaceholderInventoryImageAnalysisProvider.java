package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import com.ssafy.yumyum.domain.refrigerator.dto.ImageAnalysisType;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryImageAnalysisBatchResponse;

public class PlaceholderInventoryImageAnalysisProvider implements InventoryImageAnalysisProvider {
    @Override
    public InventoryImageAnalysisBatchResponse analyze(
            ImageAnalysisType analysisType,
            byte[] imageBytes,
            String contentType,
            String originalFilename) {
        return InventoryImageAnalysisBatchResponse.pendingProvider(analysisType);
    }
}
