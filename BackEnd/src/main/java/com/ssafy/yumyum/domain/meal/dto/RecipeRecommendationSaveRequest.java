package com.ssafy.yumyum.domain.meal.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecipeRecommendationSaveRequest(
        @NotNull @Size(min = 1, max = 10) List<@Valid RecipeRecommendationItemRequest> recipes) {
}
