package com.ssafy.yumyum.domain.admin.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.domain.admin.dto.AdminDtos.ApiUsageResponse;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.AuditLog;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.DashboardResponse;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.NoticeHistory;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.NoticeRequest;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.NoticeResponse;
import com.ssafy.yumyum.domain.admin.entity.BatchJobHistory;
import com.ssafy.yumyum.domain.admin.service.AdminService;
import com.ssafy.yumyum.domain.admin.service.ApiUsageService;
import com.ssafy.yumyum.domain.admin.service.BatchExecutionService;
import com.ssafy.yumyum.global.response.ResponseBody;
import com.ssafy.yumyum.global.response.ResponseUtil;
import com.ssafy.yumyum.global.security.service.AdminUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final ApiUsageService apiUsageService;
    private final BatchExecutionService batchExecutionService;

    @GetMapping("/dashboard")
    public ResponseBody<DashboardResponse> dashboard() {
        return ResponseUtil.createSuccessResponse(adminService.getDashboard());
    }

    @GetMapping("/api-usage")
    public ResponseBody<ApiUsageResponse> apiUsage(@RequestParam(defaultValue = "24") int hours) {
        return ResponseUtil.createSuccessResponse(apiUsageService.getHourlyUsage(hours));
    }

    @GetMapping("/batches")
    public ResponseBody<List<BatchJobHistory>> batches(@RequestParam(defaultValue = "30") int limit) {
        return ResponseUtil.createSuccessResponse(adminService.getBatchHistories(limit));
    }

    @PostMapping("/batches/expiration")
    public ResponseBody<BatchJobHistory> runExpirationBatch(@AuthenticationPrincipal AdminUserDetails admin) {
        BatchJobHistory history = batchExecutionService.runExpirationNotificationBatch("MANUAL");
        adminService.auditManualBatch(admin.getId(), history);
        return ResponseUtil.createSuccessResponse(history);
    }

    @PostMapping("/notices")
    public ResponseBody<NoticeResponse> sendNotice(@AuthenticationPrincipal AdminUserDetails admin,
            @Valid @RequestBody NoticeRequest request) {
        return ResponseUtil.createSuccessResponse(adminService.sendNotice(admin.getId(), request));
    }

    @GetMapping("/notices")
    public ResponseBody<List<NoticeHistory>> notices(@RequestParam(defaultValue = "30") int limit) {
        return ResponseUtil.createSuccessResponse(adminService.getNoticeHistories(limit));
    }

    @GetMapping("/audit-logs")
    public ResponseBody<List<AuditLog>> auditLogs(@RequestParam(defaultValue = "50") int limit) {
        return ResponseUtil.createSuccessResponse(adminService.getAuditLogs(limit));
    }
}
