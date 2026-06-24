package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import com.ssafy.yumyum.domain.refrigerator.dto.ImageAnalysisType;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryImageAnalysisResponse;

public class PlaceholderInventoryImageAnalysisProvider implements InventoryImageAnalysisProvider {
    @Override
    public InventoryImageAnalysisResponse analyze(
            ImageAnalysisType analysisType,
            byte[] imageBytes,
            String contentType,
            String originalFilename) {
        return InventoryImageAnalysisResponse.pendingProvider(analysisType);
    }
}
