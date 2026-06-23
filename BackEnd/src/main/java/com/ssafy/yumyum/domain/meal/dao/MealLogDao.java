package com.ssafy.yumyum.domain.meal.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.yumyum.domain.meal.entity.MealLog;
import com.ssafy.yumyum.domain.meal.entity.MealLogItem;

@Mapper
public interface MealLogDao {
    void insertRecommendationLog(MealLog mealLog);
    void insertRecommendationItem(MealLogItem item);
    Long findLatestRecommendationLogId(@Param("userId") Long userId);
    List<MealLogItem> findRecommendationItems(
            @Param("mealLogId") Long mealLogId,
            @Param("userId") Long userId);
}
