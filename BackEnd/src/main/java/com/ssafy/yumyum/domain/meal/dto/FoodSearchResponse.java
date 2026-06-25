package com.ssafy.yumyum.domain.meal.dto;

import java.math.BigDecimal;

import com.ssafy.yumyum.domain.meal.entity.FoodNutrition;

public record FoodSearchResponse(
        String foodCode,
        String name,
        String category,
        String baseAmount,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbohydrate,
        BigDecimal fat
) {
    public static FoodSearchResponse from(FoodNutrition food) {
        return new FoodSearchResponse(
                food.getCode(),
                food.getName(),
                food.getCategory(),
                food.getWeight(),
                food.getEnergyKcal(),
                food.getProteinG(),
                food.getCarbohydrateG(),
                food.getFatG()
        );
    }
}
