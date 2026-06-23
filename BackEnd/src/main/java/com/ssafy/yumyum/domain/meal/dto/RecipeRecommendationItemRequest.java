package com.ssafy.yumyum.domain.meal.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecipeRecommendationItemRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @Min(0) @Max(100) int matchRate,
        @Min(1) int cookTime,
        @Min(1) int servings,
        @NotBlank @Size(max = 30) String difficulty,
        @Min(0) int calories,
        @NotNull List<@NotBlank String> availableIngredients,
        @NotNull List<@NotBlank String> missingIngredients,
        @NotNull @Size(min = 1) List<@NotBlank String> steps,
        @NotNull List<@NotBlank String> tips) {
}
