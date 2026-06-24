package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record PororoOcrProperties(
        @Value("${pororo.ocr.url:http://localhost:8001/ocr}") String url,
        @Value("${pororo.ocr.lang:ko}") String lang
) {
    public boolean isConfigured() {
        return url != null && !url.isBlank();
    }
}
