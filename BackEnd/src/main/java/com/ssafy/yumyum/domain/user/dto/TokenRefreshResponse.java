package com.ssafy.yumyum.domain.user.dto;

public record TokenRefreshResponse(String accessToken, String refreshToken, String role) {
}
