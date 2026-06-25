package com.ssafy.yumyum.domain.meal.entity;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MealDailySummary {
    private Long mealCount;
    private Long itemCount;
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalCarbohydrate;
    private BigDecimal totalFat;
}
