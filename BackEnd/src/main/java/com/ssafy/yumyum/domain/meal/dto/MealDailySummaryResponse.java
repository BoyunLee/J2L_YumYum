package com.ssafy.yumyum.domain.meal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MealDailySummaryResponse(
        LocalDate date,
        Long mealCount,
        Long itemCount,
        BigDecimal totalCalories,
        BigDecimal totalProtein,
        BigDecimal totalCarbohydrate,
        BigDecimal totalFat,
        BigDecimal goalCalories,
        BigDecimal goalProtein,
        BigDecimal goalCarbohydrate,
        BigDecimal goalFat
) {
}
