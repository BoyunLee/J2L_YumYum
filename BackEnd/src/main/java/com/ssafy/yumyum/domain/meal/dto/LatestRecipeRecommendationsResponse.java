package com.ssafy.yumyum.domain.meal.dto;

import java.util.List;

public record LatestRecipeRecommendationsResponse(
        Long mealLogId,
        List<SavedRecipeRecommendationResponse> recipes) {
}
