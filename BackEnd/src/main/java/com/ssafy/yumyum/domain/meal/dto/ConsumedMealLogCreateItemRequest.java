package com.ssafy.yumyum.domain.meal.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConsumedMealLogCreateItemRequest(
        @NotBlank String foodCode,
        @NotNull @DecimalMin("0.1") BigDecimal quantity
) {
}
