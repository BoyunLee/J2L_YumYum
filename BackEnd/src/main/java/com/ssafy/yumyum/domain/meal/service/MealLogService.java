package com.ssafy.yumyum.domain.meal.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.yumyum.domain.meal.dao.MealLogDao;
import com.ssafy.yumyum.domain.meal.dto.LatestRecipeRecommendationsResponse;
import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationItemRequest;
import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationSaveRequest;
import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationSaveResponse;
import com.ssafy.yumyum.domain.meal.dto.SavedRecipeRecommendationResponse;
import com.ssafy.yumyum.domain.meal.entity.MealLog;
import com.ssafy.yumyum.domain.meal.entity.MealLogItem;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MealLogService {
    private final MealLogDao mealLogDao;
    private final ObjectMapper objectMapper;

    @Transactional
    public RecipeRecommendationSaveResponse saveRecommendations(
            Long userId,
            RecipeRecommendationSaveRequest request) {
        MealLog mealLog = new MealLog();
        mealLog.setUserId(userId);
        mealLogDao.insertRecommendationLog(mealLog);

        List<Long> itemIds = new ArrayList<>();
        for (RecipeRecommendationItemRequest recipe : request.recipes()) {
            MealLogItem item = new MealLogItem();
            item.setMealLogId(mealLog.getId());
            item.setName(recipe.name().trim());
            item.setQuantity(BigDecimal.valueOf(recipe.servings()));
            item.setUnit("인분");
            item.setRecipePayload(toJson(recipe));
            mealLogDao.insertRecommendationItem(item);
            itemIds.add(item.getId());
        }

        return new RecipeRecommendationSaveResponse(mealLog.getId(), List.copyOf(itemIds));
    }

    @Transactional(readOnly = true)
    public LatestRecipeRecommendationsResponse findLatestRecommendations(Long userId) {
        Long mealLogId = mealLogDao.findLatestRecommendationLogId(userId);
        if (mealLogId == null) {
            return new LatestRecipeRecommendationsResponse(null, List.of());
        }

        List<SavedRecipeRecommendationResponse> recipes = mealLogDao
                .findRecommendationItems(mealLogId, userId)
                .stream()
                .map(item -> SavedRecipeRecommendationResponse.from(
                        item.getId(),
                        fromJson(item.getRecipePayload())))
                .toList();
        return new LatestRecipeRecommendationsResponse(mealLogId, recipes);
    }

    private String toJson(RecipeRecommendationItemRequest recipe) {
        try {
            return objectMapper.writeValueAsString(recipe);
        } catch (JacksonException exception) {
            throw new IllegalStateException("레시피 추천 데이터를 JSON으로 변환하지 못했습니다.", exception);
        }
    }

    private RecipeRecommendationItemRequest fromJson(String recipePayload) {
        try {
            return objectMapper.readValue(recipePayload, RecipeRecommendationItemRequest.class);
        } catch (JacksonException exception) {
            throw new IllegalStateException("저장된 레시피 추천 데이터를 읽지 못했습니다.", exception);
        }
    }
}
