package com.ssafy.yumyum.domain.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.ssafy.yumyum.domain.admin.entity.AdminRole;
import com.ssafy.yumyum.domain.admin.entity.BatchJobHistory;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AdminDtos {
    private AdminDtos() {
    }

    public record LoginRequest(
            @NotBlank @Size(max = 100) String loginId,
            @NotBlank @Size(max = 100) String password) {
    }

    public record Profile(Long id, String loginId, AdminRole role) {
    }

    public record LoginResponse(String accessToken, long expiresIn, Profile admin) {
    }

    public record DashboardSummary(
            long totalUsers,
            long newUsersToday,
            long activeUsers7Days,
            long totalInventory,
            long apiCallsToday) {
    }

    public record RecentUser(
            Long id,
            String nickname,
            String email,
            String provider,
            LocalDateTime createdAt,
            LocalDateTime lastLoginAt) {
    }

    public record DashboardResponse(
            DashboardSummary summary,
            List<RecentUser> recentUsers,
            BatchJobHistory latestBatch) {
    }

    public record ApiUsagePoint(LocalDateTime hour, long ocr, long barcode, long recipeRecommendation) {
    }

    public record ApiUsageResponse(
            int hours,
            long totalCalls,
            long successfulCalls,
            List<ApiUsagePoint> points) {
    }

    public record RecipeUsageRequest(
            boolean success,
            @Min(0) @Max(300000) long durationMs,
            @Size(max = 50) String errorCode) {
    }

    public record NoticeRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 2000) String content) {
    }

    public record NoticeResponse(Long historyId, int sentCount) {
    }

    public record NoticeHistory(
            Long id,
            String adminLoginId,
            String title,
            String content,
            int sentCount,
            LocalDateTime createdAt) {
    }

    public record AuditLog(
            Long id,
            String adminLoginId,
            String actionType,
            String targetType,
            String targetId,
            String actionDetail,
            String noticeTitle,
            String noticeContent,
            LocalDateTime createdAt) {
    }
}
