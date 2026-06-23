package com.ssafy.yumyum.domain.meal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationSaveRequest;
import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationSaveResponse;
import com.ssafy.yumyum.domain.meal.dto.LatestRecipeRecommendationsResponse;
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

    @PostMapping("/recommendations")
    public ResponseEntity<ResponseBody<RecipeRecommendationSaveResponse>> saveRecommendations(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody RecipeRecommendationSaveRequest request) {
        RecipeRecommendationSaveResponse response = mealLogService.saveRecommendations(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.createSuccessResponse(response));
    }

    @GetMapping("/recommendations/latest")
    public ResponseBody<LatestRecipeRecommendationsResponse> findLatestRecommendations(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseUtil.createSuccessResponse(mealLogService.findLatestRecommendations(user.getId()));
    }
}
