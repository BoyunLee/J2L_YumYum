package com.ssafy.yumyum.domain.admin.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.yumyum.domain.admin.dao.AdminDao;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.AuditLog;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.DashboardResponse;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.DashboardSummary;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.LoginResponse;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.NoticeHistory;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.NoticeRequest;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.NoticeResponse;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.Profile;
import com.ssafy.yumyum.domain.admin.entity.AdminUser;
import com.ssafy.yumyum.domain.admin.entity.BatchJobHistory;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;
import com.ssafy.yumyum.global.security.jwt.JwtProperties;
import com.ssafy.yumyum.global.security.jwt.TokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminDao adminDao;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public LoginResponse login(String loginId, String password) {
        AdminUser admin = adminDao.findAdminByLoginId(loginId.trim())
                .orElseThrow(() -> new BusinessException(ExceptionType.INVALID_LOGIN));
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new BusinessException(ExceptionType.INVALID_LOGIN);
        }
        adminDao.insertAdminLog(admin.getId(), "LOGIN", "ADMIN", String.valueOf(admin.getId()), null);
        return new LoginResponse(tokenProvider.generateAdminAccessToken(admin),
                jwtProperties.getAccessExpiredAt(), profile(admin));
    }

    @Transactional(readOnly = true)
    public Profile getProfile(Long adminId) {
        return profile(findAdmin(adminId));
    }

    @Transactional
    public void logout(Long adminId) {
        adminDao.insertAdminLog(adminId, "LOGOUT", "ADMIN", String.valueOf(adminId), null);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        DashboardSummary summary = new DashboardSummary(
                adminDao.countTotalUsers(), adminDao.countNewUsersToday(), adminDao.countActiveUsers7Days(),
                adminDao.countTotalInventory(), adminDao.countApiCallsToday());
        return new DashboardResponse(summary, adminDao.findRecentUsers(6), adminDao.findLatestBatchHistory());
    }

    @Transactional(readOnly = true)
    public List<BatchJobHistory> getBatchHistories(int limit) {
        return adminDao.findBatchHistories(boundedLimit(limit));
    }

    @Transactional
    public NoticeResponse sendNotice(Long adminId, NoticeRequest request) {
        String title = request.title().trim();
        String content = request.content().trim();
        int sentCount = adminDao.insertNoticeForAllUsers(title, content);
        adminDao.insertAdminPushHistory(adminId, title, content, sentCount);
        Long historyId = adminDao.findLastInsertedId();
        adminDao.insertAdminLog(adminId, "SEND_NOTICE", "ADMIN_PUSH", String.valueOf(historyId),
                "탈퇴하지 않은 전체 사용자 " + sentCount + "명에게 공지 발송");
        return new NoticeResponse(historyId, sentCount);
    }

    @Transactional(readOnly = true)
    public List<NoticeHistory> getNoticeHistories(int limit) {
        return adminDao.findNoticeHistories(boundedLimit(limit));
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogs(int limit) {
        return adminDao.findAuditLogs(boundedLimit(limit));
    }

    @Transactional
    public void auditManualBatch(Long adminId, BatchJobHistory history) {
        adminDao.insertAdminLog(adminId, "RUN_BATCH", "BATCH_JOB", String.valueOf(history.getId()),
                "유통기한 알림 " + history.getProcessedCount() + "건 생성");
    }

    private AdminUser findAdmin(Long adminId) {
        return adminDao.findAdminById(adminId)
                .orElseThrow(() -> new BusinessException(ExceptionType.INVALID_LOGIN));
    }

    private Profile profile(AdminUser admin) {
        return new Profile(admin.getId(), admin.getLoginId(), admin.getRole());
    }

    private int boundedLimit(int limit) {
        return Math.max(1, Math.min(100, limit));
    }
}
