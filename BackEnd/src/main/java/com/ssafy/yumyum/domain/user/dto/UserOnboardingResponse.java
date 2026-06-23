package com.ssafy.yumyum.domain.user.dto;

public record UserOnboardingResponse(String accessToken, String refreshToken, String role) {
}
