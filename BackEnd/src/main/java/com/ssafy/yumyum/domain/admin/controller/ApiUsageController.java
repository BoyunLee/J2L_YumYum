package com.ssafy.yumyum.domain.admin.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.domain.admin.dto.AdminDtos.RecipeUsageRequest;
import com.ssafy.yumyum.domain.admin.service.ApiUsageService;
import com.ssafy.yumyum.global.response.ResponseBody;
import com.ssafy.yumyum.global.response.ResponseUtil;
import com.ssafy.yumyum.global.security.service.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class ApiUsageController {
    private final ApiUsageService apiUsageService;

    @PostMapping("/recipe-recommendation")
    public ResponseBody<Void> recordRecipeRecommendation(@AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody RecipeUsageRequest request) {
        apiUsageService.record(user.getId(), ApiUsageService.RECIPE_RECOMMENDATION,
                request.success(), request.durationMs(), request.errorCode());
        return ResponseUtil.createSuccessResponse();
    }
}
