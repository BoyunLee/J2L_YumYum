package com.ssafy.yumyum.domain.meal.dao;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.yumyum.domain.meal.entity.FoodNutrition;
import com.ssafy.yumyum.domain.meal.entity.MealLog;
import com.ssafy.yumyum.domain.meal.entity.MealDailySummary;
import com.ssafy.yumyum.domain.meal.entity.MealLogItem;

@Mapper
public interface MealLogDao {
    List<FoodNutrition> searchFoods(@Param("query") String query, @Param("limit") int limit);
    FoodNutrition findFoodByCode(@Param("foodCode") String foodCode);

    void insertConsumedLog(MealLog mealLog);
    void insertConsumedItem(MealLogItem item);
    List<MealLog> findConsumedLogs(@Param("userId") Long userId, @Param("date") LocalDate date);
    MealLog findConsumedLogByIdAndUserId(@Param("mealLogId") Long mealLogId, @Param("userId") Long userId);
    List<MealLogItem> findConsumedItemsByMealLogId(@Param("mealLogId") Long mealLogId);
    MealDailySummary findConsumedSummaryByDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    int deleteConsumedItemsByMealLogIdAndUserId(@Param("mealLogId") Long mealLogId, @Param("userId") Long userId);
    int deleteConsumedLogByIdAndUserId(@Param("mealLogId") Long mealLogId, @Param("userId") Long userId);

    void insertRecommendationLog(MealLog mealLog);
    void insertRecommendationItem(MealLogItem item);
    Long findLatestRecommendationLogId(@Param("userId") Long userId);
    List<MealLogItem> findRecommendationItems(
            @Param("mealLogId") Long mealLogId,
            @Param("userId") Long userId);
}
