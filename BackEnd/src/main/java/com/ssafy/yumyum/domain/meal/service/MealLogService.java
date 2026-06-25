package com.ssafy.yumyum.domain.meal.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.yumyum.domain.meal.dao.MealLogDao;
import com.ssafy.yumyum.domain.meal.dto.ConsumedMealLogCreateItemRequest;
import com.ssafy.yumyum.domain.meal.dto.ConsumedMealLogCreateRequest;
import com.ssafy.yumyum.domain.meal.dto.FoodSearchResponse;
import com.ssafy.yumyum.domain.meal.dto.LatestRecipeRecommendationsResponse;
import com.ssafy.yumyum.domain.meal.dto.MealDailySummaryResponse;
import com.ssafy.yumyum.domain.meal.dto.MealLogItemResponse;
import com.ssafy.yumyum.domain.meal.dto.MealLogResponse;
import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationItemRequest;
import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationSaveRequest;
import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationSaveResponse;
import com.ssafy.yumyum.domain.meal.dto.SavedRecipeRecommendationResponse;
import com.ssafy.yumyum.domain.meal.entity.FoodNutrition;
import com.ssafy.yumyum.domain.meal.entity.MealDailySummary;
import com.ssafy.yumyum.domain.meal.entity.MealLog;
import com.ssafy.yumyum.domain.meal.entity.MealLogItem;
import com.ssafy.yumyum.domain.meal.entity.MealType;
import com.ssafy.yumyum.domain.meal.service.recommendation.RecipeRecommendationProvider;
import com.ssafy.yumyum.domain.admin.service.ApiUsageService;
import com.ssafy.yumyum.domain.refrigerator.dao.RefrigeratorItemDao;
import com.ssafy.yumyum.domain.refrigerator.entity.RefrigeratorItem;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MealLogService {
    private static final BigDecimal DEFAULT_ENERGY_GOAL = BigDecimal.valueOf(2000);
    private static final BigDecimal DEFAULT_PROTEIN_GOAL = BigDecimal.valueOf(100);
    private static final BigDecimal DEFAULT_CARBOHYDRATE_GOAL = BigDecimal.valueOf(250);
    private static final BigDecimal DEFAULT_FAT_GOAL = BigDecimal.valueOf(65);

    private final MealLogDao mealLogDao;
    private final ObjectMapper objectMapper;
    private final RefrigeratorItemDao refrigeratorItemDao;
    private final RecipeRecommendationProvider recipeRecommendationProvider;
    private final ApiUsageService apiUsageService;

    @Transactional(readOnly = true)
    public List<FoodSearchResponse> searchFoods(String query, int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 20));
        return mealLogDao.searchFoods(trimToNull(query), normalizedLimit)
                .stream()
                .map(FoodSearchResponse::from)
                .toList();
    }

    @Transactional
    public MealLogResponse createConsumedMealLog(Long userId, ConsumedMealLogCreateRequest request) {
        MealLog mealLog = new MealLog();
        mealLog.setUserId(userId);
        mealLog.setMealType(request.mealType());
        mealLog.setEatenAt(defaultEatenAt(request.date(), request.mealType()));
        mealLog.setMemo(trimToNull(request.memo()));
        mealLogDao.insertConsumedLog(mealLog);

        for (ConsumedMealLogCreateItemRequest itemRequest : request.items()) {
            createConsumedItem(mealLog.getId(), itemRequest);
        }

        MealLog savedMealLog = mealLogDao.findConsumedLogByIdAndUserId(mealLog.getId(), userId);
        if (savedMealLog == null) {
            throw new BusinessException(ExceptionType.MEAL_LOG_NOT_FOUND);
        }

        savedMealLog.setItems(mealLogDao.findConsumedItemsByMealLogId(savedMealLog.getId()));
        return toMealLogResponse(savedMealLog);
    }

    @Transactional(readOnly = true)
    public List<MealLogResponse> findConsumedMealLogs(Long userId, LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        List<MealLog> mealLogs = mealLogDao.findConsumedLogs(userId, targetDate);
        for (MealLog mealLog : mealLogs) {
            mealLog.setItems(mealLogDao.findConsumedItemsByMealLogId(mealLog.getId()));
        }
        return mealLogs.stream()
                .map(this::toMealLogResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MealDailySummaryResponse findConsumedSummary(Long userId, LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        MealDailySummary summary = mealLogDao.findConsumedSummaryByDate(userId, targetDate);
        if (summary == null) {
            summary = new MealDailySummary();
        }

        return new MealDailySummaryResponse(
                targetDate,
                summary.getMealCount() == null ? 0L : summary.getMealCount(),
                summary.getItemCount() == null ? 0L : summary.getItemCount(),
                safe(summary.getTotalCalories()),
                safe(summary.getTotalProtein()),
                safe(summary.getTotalCarbohydrate()),
                safe(summary.getTotalFat()),
                DEFAULT_ENERGY_GOAL,
                DEFAULT_PROTEIN_GOAL,
                DEFAULT_CARBOHYDRATE_GOAL,
                DEFAULT_FAT_GOAL);
    }

    @Transactional
    public void deleteConsumedMealLog(Long userId, Long mealLogId) {
        MealLog mealLog = mealLogDao.findConsumedLogByIdAndUserId(mealLogId, userId);
        if (mealLog == null) {
            throw new BusinessException(ExceptionType.MEAL_LOG_NOT_FOUND);
        }

        mealLogDao.deleteConsumedItemsByMealLogIdAndUserId(mealLogId, userId);
        mealLogDao.deleteConsumedLogByIdAndUserId(mealLogId, userId);
    }

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

    public LatestRecipeRecommendationsResponse generateRecommendations(Long userId) {
        long startedAt = System.nanoTime();
        boolean success = false;
        String errorCode = null;
        try {
            List<RefrigeratorItem> inventory = refrigeratorItemDao.findAllByUserId(userId);
            if (inventory.isEmpty()) {
                throw new BusinessException(ExceptionType.RECIPE_RECOMMENDATION_INVENTORY_EMPTY);
            }

            List<RecipeRecommendationItemRequest> recipes = recipeRecommendationProvider.recommend(inventory);
            if (recipes.isEmpty()) {
                throw new BusinessException(ExceptionType.RECIPE_RECOMMENDATION_FAILED);
            }

            saveRecommendations(userId, new RecipeRecommendationSaveRequest(recipes));
            success = true;
            return findLatestRecommendations(userId);
        } catch (BusinessException exception) {
            errorCode = exception.getExceptionType().getCode();
            throw exception;
        } catch (RuntimeException exception) {
            errorCode = "REQUEST_FAILED";
            throw new BusinessException(ExceptionType.RECIPE_RECOMMENDATION_FAILED);
        } finally {
            recordRecipeRecommendationUsage(
                    userId,
                    success,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt),
                    errorCode
            );
        }
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

    private void createConsumedItem(Long mealLogId, ConsumedMealLogCreateItemRequest itemRequest) {
        FoodNutrition food = mealLogDao.findFoodByCode(itemRequest.foodCode());
        if (food == null) {
            throw new BusinessException(ExceptionType.FOOD_NUTRITION_NOT_FOUND);
        }

        MealLogItem item = new MealLogItem();
        item.setMealLogId(mealLogId);
        item.setFoodCode(food.getCode());
        item.setName(food.getName());
        item.setQuantity(itemRequest.quantity());
        item.setUnit(food.getWeight());
        mealLogDao.insertConsumedItem(item);
    }

    private MealLogResponse toMealLogResponse(MealLog mealLog) {
        List<MealLogItemResponse> items = mealLog.getItems()
                .stream()
                .map(this::toMealLogItemResponse)
                .toList();

        return new MealLogResponse(
                mealLog.getId(),
                mealLog.getMealType(),
                mealLog.getEatenAt().toLocalDate(),
                mealLog.getMemo(),
                sum(items.stream().map(MealLogItemResponse::calories).toList()),
                sum(items.stream().map(MealLogItemResponse::protein).toList()),
                sum(items.stream().map(MealLogItemResponse::carbohydrate).toList()),
                sum(items.stream().map(MealLogItemResponse::fat).toList()),
                items);
    }

    private MealLogItemResponse toMealLogItemResponse(MealLogItem item) {
        return new MealLogItemResponse(
                item.getId(),
                item.getFoodCode(),
                item.getName(),
                safe(item.getQuantity()),
                item.getUnit(),
                multiply(item.getEnergyKcal(), item.getQuantity()),
                multiply(item.getProteinG(), item.getQuantity()),
                multiply(item.getCarbohydrateG(), item.getQuantity()),
                multiply(item.getFatG(), item.getQuantity()));
    }

    private LocalDateTime defaultEatenAt(LocalDate date, MealType mealType) {
        LocalTime time = switch (mealType) {
            case BREAKFAST -> LocalTime.of(8, 0);
            case LUNCH -> LocalTime.of(12, 30);
            case DINNER -> LocalTime.of(18, 30);
            case SNACK -> LocalTime.of(15, 0);
        };
        return LocalDateTime.of(date, time);
    }

    private BigDecimal multiply(BigDecimal value, BigDecimal quantity) {
        return safe(value).multiply(safe(quantity));
    }

    private BigDecimal sum(List<BigDecimal> values) {
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            result = result.add(safe(value));
        }
        return result;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String toJson(RecipeRecommendationItemRequest recipe) {
        try {
            return objectMapper.writeValueAsString(recipe);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("레시피 추천 데이터를 JSON으로 변환하지 못했습니다.", exception);
        }
    }

    private RecipeRecommendationItemRequest fromJson(String recipePayload) {
        try {
            return objectMapper.readValue(recipePayload, RecipeRecommendationItemRequest.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("저장된 레시피 추천 데이터를 읽지 못했습니다.", exception);
        }
    }

    private void recordRecipeRecommendationUsage(Long userId, boolean success, long durationMs, String errorCode) {
        try {
            apiUsageService.record(
                    userId,
                    ApiUsageService.RECIPE_RECOMMENDATION,
                    success,
                    durationMs,
                    errorCode
            );
        } catch (RuntimeException exception) {
            // 통계 기록 실패가 사용자 레시피 요청 결과를 덮어쓰지 않도록 합니다.
        }
    }
}
