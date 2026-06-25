package com.ssafy.yumyum.domain.meal.dto;

import java.time.LocalDate;
import java.util.List;

import com.ssafy.yumyum.domain.meal.entity.MealType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConsumedMealLogCreateRequest(
        @NotNull MealType mealType,
        @NotNull LocalDate date,
        @Size(max = 500) String memo,
        @NotEmpty List<@Valid ConsumedMealLogCreateItemRequest> items
) {
}
