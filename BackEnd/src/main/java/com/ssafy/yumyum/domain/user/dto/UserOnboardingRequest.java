package com.ssafy.yumyum.domain.user.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ssafy.yumyum.domain.user.entity.ActivityLevel;
import com.ssafy.yumyum.domain.user.entity.Gender;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record UserOnboardingRequest(
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(max = 100, message = "닉네임은 100자 이하로 입력해 주세요.") String nickname,
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 255, message = "이메일은 255자 이하로 입력해 주세요.") String email,
        @NotNull(message = "성별을 선택해 주세요.") Gender gender,
        @NotNull(message = "생년월일을 입력해 주세요.")
        @Past(message = "생년월일은 과거 날짜여야 합니다.") LocalDate birthDate,
        @NotNull(message = "키를 입력해 주세요.")
        @DecimalMin(value = "50.0", message = "키는 50cm 이상이어야 합니다.")
        @DecimalMax(value = "300.0", message = "키는 300cm 이하여야 합니다.") BigDecimal heightCm,
        @NotNull(message = "몸무게를 입력해 주세요.")
        @DecimalMin(value = "10.0", message = "몸무게는 10kg 이상이어야 합니다.")
        @DecimalMax(value = "500.0", message = "몸무게는 500kg 이하여야 합니다.") BigDecimal weightKg,
        @NotNull(message = "활동량을 선택해 주세요.") ActivityLevel activityLevel
) {
}
