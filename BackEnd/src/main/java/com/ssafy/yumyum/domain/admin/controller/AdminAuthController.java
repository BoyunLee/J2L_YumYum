package com.ssafy.yumyum.domain.admin.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.domain.admin.dto.AdminDtos.LoginRequest;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.LoginResponse;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.Profile;
import com.ssafy.yumyum.domain.admin.service.AdminService;
import com.ssafy.yumyum.global.response.ResponseBody;
import com.ssafy.yumyum.global.response.ResponseUtil;
import com.ssafy.yumyum.global.security.service.AdminUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseBody<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseUtil.createSuccessResponse(adminService.login(request.loginId(), request.password()));
    }

    @GetMapping("/me")
    public ResponseBody<Profile> me(@AuthenticationPrincipal AdminUserDetails admin) {
        return ResponseUtil.createSuccessResponse(adminService.getProfile(admin.getId()));
    }

    @PostMapping("/logout")
    public ResponseBody<Void> logout(@AuthenticationPrincipal AdminUserDetails admin) {
        adminService.logout(admin.getId());
        return ResponseUtil.createSuccessResponse();
    }
}
