package com.ssafy.yumyum.global.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record IntegrationModeProperties(@Value("${integration.mode:live}") String mode) {
    public boolean isMock() {
        return normalizedMode() == Mode.MOCK;
    }

    public boolean isLive() {
        return normalizedMode() == Mode.LIVE;
    }

    public boolean isFallback() {
        return normalizedMode() == Mode.FALLBACK;
    }

    public Mode normalizedMode() {
        if (mode == null || mode.isBlank()) {
            return Mode.LIVE;
        }
        try {
            return Mode.valueOf(mode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Mode.LIVE;
        }
    }

    public enum Mode {
        MOCK,
        LIVE,
        FALLBACK
    }
}
