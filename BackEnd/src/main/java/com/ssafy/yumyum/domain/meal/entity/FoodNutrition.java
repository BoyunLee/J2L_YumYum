package com.ssafy.yumyum.domain.meal.entity;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoodNutrition {
    private String code;
    private String name;
    private String category;
    private String weight;
    private BigDecimal energyKcal;
    private BigDecimal proteinG;
    private BigDecimal fatG;
    private BigDecimal carbohydrateG;
}
