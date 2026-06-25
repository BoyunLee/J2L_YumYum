package com.ssafy.yumyum.domain.meal.service.recommendation;

import java.util.List;

import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationItemRequest;
import com.ssafy.yumyum.domain.refrigerator.entity.RefrigeratorItem;

public interface RecipeRecommendationProvider {
    List<RecipeRecommendationItemRequest> recommend(List<RefrigeratorItem> inventory);
}
