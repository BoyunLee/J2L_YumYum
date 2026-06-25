package com.ssafy.yumyum.domain.meal.dto;

import java.math.BigDecimal;

public record MealLogItemResponse(
        Long id,
        String foodCode,
        String name,
        BigDecimal quantity,
        String baseAmount,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbohydrate,
        BigDecimal fat
) {
}
