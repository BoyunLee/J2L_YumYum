package com.ssafy.yumyum.domain.refrigerator.dto;

import java.util.Locale;

import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

public enum ImageAnalysisType {
    OCR,
    BARCODE;

    public static ImageAnalysisType from(String value) {
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BusinessException(ExceptionType.INVALID_IMAGE_ANALYSIS_TYPE);
        }
    }
}
