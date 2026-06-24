package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import com.ssafy.yumyum.domain.refrigerator.dto.ImageAnalysisType;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryImageAnalysisBatchResponse;

/**
 * 외부 OCR/바코드 API를 연결하는 교체 지점입니다.
 *
 * 구현체를 Spring Bean으로 등록하면 기본 placeholder 구현은 자동으로 비활성화됩니다.
 * 업체별 응답을 InventoryImageAnalysisResponse로 변환하면서 카테고리, 보관 위치와 단위를
 * 애플리케이션 값으로 정규화해 주세요.
 */
public interface InventoryImageAnalysisProvider {
    InventoryImageAnalysisBatchResponse analyze(
            ImageAnalysisType analysisType,
            byte[] imageBytes,
            String contentType,
            String originalFilename
    );
}
