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

public record UserProfileUpdateRequest(
        @NotBlank @Size(max = 100) String nickname,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull Gender gender,
        @NotNull @Past LocalDate birthDate,
        @NotNull @DecimalMin("50.0") @DecimalMax("300.0") BigDecimal heightCm,
        @NotNull @DecimalMin("10.0") @DecimalMax("500.0") BigDecimal weightKg,
        @NotNull ActivityLevel activityLevel
) {
}
