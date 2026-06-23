package com.ssafy.yumyum.domain.user.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ssafy.yumyum.domain.user.entity.ActivityLevel;
import com.ssafy.yumyum.domain.user.entity.Gender;
import com.ssafy.yumyum.global.security.oauth2.user.OAuth2Provider;

public record UserProfileResponse(
        Long id,
        String nickname,
        String email,
        OAuth2Provider provider,
        String profileImageUrl,
        Gender gender,
        LocalDate birthDate,
        BigDecimal heightCm,
        BigDecimal weightKg,
        ActivityLevel activityLevel
) {
}
