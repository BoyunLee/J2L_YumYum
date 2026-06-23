package com.ssafy.yumyum.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.domain.user.dto.UserOnboardingRequest;
import com.ssafy.yumyum.domain.user.dto.UserOnboardingResponse;
import com.ssafy.yumyum.domain.user.dto.UserProfileResponse;
import com.ssafy.yumyum.domain.user.dto.UserProfileUpdateRequest;
import com.ssafy.yumyum.domain.user.service.UserService;
import com.ssafy.yumyum.global.response.ResponseBody;
import com.ssafy.yumyum.global.response.ResponseUtil;
import com.ssafy.yumyum.global.security.service.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ResponseBody<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(userService.getProfile(user.getId())));
    }

    @PatchMapping("/me")
    public ResponseEntity<ResponseBody<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(userService.updateProfile(user.getId(), request)));
    }

    @PatchMapping("/me/onboarding")
    public ResponseEntity<ResponseBody<UserOnboardingResponse>> completeOnboarding(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody UserOnboardingRequest request) {
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(
                userService.completeOnboarding(user.getId(), request)
        ));
    }
}
