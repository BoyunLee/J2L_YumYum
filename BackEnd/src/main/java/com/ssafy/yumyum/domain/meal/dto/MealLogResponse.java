package com.ssafy.yumyum.domain.meal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.ssafy.yumyum.domain.meal.entity.MealType;

public record MealLogResponse(
        Long id,
        MealType mealType,
        LocalDate date,
        String memo,
        BigDecimal totalCalories,
        BigDecimal totalProtein,
        BigDecimal totalCarbohydrate,
        BigDecimal totalFat,
        List<MealLogItemResponse> items
) {
}
