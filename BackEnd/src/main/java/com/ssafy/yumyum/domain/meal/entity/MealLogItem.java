package com.ssafy.yumyum.domain.meal.entity;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MealLogItem {
    private Long id;
    private Long mealLogId;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private String recipePayload;
}
