package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record GmsOcrProperties(
        @Value("${gms.api.url}") String url,
        @Value("${gms.api.model:gpt-5-nano}") String model,
        @Value("${gms.api.key}") String apiKey
) {
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
