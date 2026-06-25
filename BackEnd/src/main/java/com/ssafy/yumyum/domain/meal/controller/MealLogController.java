package com.ssafy.yumyum.domain.meal.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.domain.meal.dto.ConsumedMealLogCreateRequest;
import com.ssafy.yumyum.domain.meal.dto.FoodSearchResponse;
import com.ssafy.yumyum.domain.meal.dto.LatestRecipeRecommendationsResponse;
import com.ssafy.yumyum.domain.meal.dto.MealDailySummaryResponse;
import com.ssafy.yumyum.domain.meal.dto.MealLogResponse;
import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationSaveRequest;
import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationSaveResponse;
import com.ssafy.yumyum.domain.meal.service.MealLogService;
import com.ssafy.yumyum.global.response.ResponseBody;
import com.ssafy.yumyum.global.response.ResponseUtil;
import com.ssafy.yumyum.global.security.service.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/meal-logs")
@RequiredArgsConstructor
public class MealLogController {
    private final MealLogService mealLogService;

    @GetMapping("/foods/search")
    public ResponseBody<List<FoodSearchResponse>> searchFoods(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "limit", defaultValue = "8") int limit) {
        return ResponseUtil.createSuccessResponse(mealLogService.searchFoods(query, limit));
    }

    @PostMapping
    public ResponseEntity<ResponseBody<MealLogResponse>> createMealLog(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ConsumedMealLogCreateRequest request) {
        MealLogResponse response = mealLogService.createConsumedMealLog(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.createSuccessResponse(response));
    }

    @GetMapping
    public ResponseBody<List<MealLogResponse>> findMealLogs(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseUtil.createSuccessResponse(mealLogService.findConsumedMealLogs(user.getId(), date));
    }

    @GetMapping("/summary")
    public ResponseBody<MealDailySummaryResponse> findDailySummary(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseUtil.createSuccessResponse(mealLogService.findConsumedSummary(user.getId(), date));
    }

    @DeleteMapping("/{mealLogId}")
    public ResponseBody<Void> deleteMealLog(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long mealLogId) {
        mealLogService.deleteConsumedMealLog(user.getId(), mealLogId);
        return ResponseUtil.createSuccessResponse();
    }

    @PostMapping("/recommendations")
    public ResponseEntity<ResponseBody<RecipeRecommendationSaveResponse>> saveRecommendations(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody RecipeRecommendationSaveRequest request) {
        RecipeRecommendationSaveResponse response = mealLogService.saveRecommendations(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.createSuccessResponse(response));
    }

    @PostMapping("/recommendations/generate")
    public ResponseEntity<ResponseBody<LatestRecipeRecommendationsResponse>> generateRecommendations(
            @AuthenticationPrincipal CustomUserDetails user) {
        LatestRecipeRecommendationsResponse response = mealLogService.generateRecommendations(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.createSuccessResponse(response));
    }

    @GetMapping("/recommendations/latest")
    public ResponseBody<LatestRecipeRecommendationsResponse> findLatestRecommendations(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseUtil.createSuccessResponse(mealLogService.findLatestRecommendations(user.getId()));
    }
}
