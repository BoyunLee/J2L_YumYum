package com.ssafy.yumyum.domain.meal.dto;

import java.util.List;

public record SavedRecipeRecommendationResponse(
        Long id,
        String name,
        String description,
        int matchRate,
        int cookTime,
        int servings,
        String difficulty,
        int calories,
        List<String> availableIngredients,
        List<String> missingIngredients,
        List<String> steps,
        List<String> tips) {

    public static SavedRecipeRecommendationResponse from(
            Long id,
            RecipeRecommendationItemRequest recipe) {
        return new SavedRecipeRecommendationResponse(
                id,
                recipe.name(),
                recipe.description(),
                recipe.matchRate(),
                recipe.cookTime(),
                recipe.servings(),
                recipe.difficulty(),
                recipe.calories(),
                recipe.availableIngredients(),
                recipe.missingIngredients(),
                recipe.steps(),
                recipe.tips());
    }
}
