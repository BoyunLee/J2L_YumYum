package com.ssafy.yumyum.domain.meal.dto;

import java.util.List;

public record RecipeRecommendationSaveResponse(Long mealLogId, List<Long> mealLogItemIds) {
}
