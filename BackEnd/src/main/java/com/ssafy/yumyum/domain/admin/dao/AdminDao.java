package com.ssafy.yumyum.domain.admin.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.yumyum.domain.admin.dto.AdminDtos.AuditLog;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.NoticeHistory;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.RecentUser;
import com.ssafy.yumyum.domain.admin.dto.ApiUsageAggregate;
import com.ssafy.yumyum.domain.admin.entity.AdminUser;
import com.ssafy.yumyum.domain.admin.entity.ApiUsageLog;
import com.ssafy.yumyum.domain.admin.entity.BatchJobHistory;

@Mapper
public interface AdminDao {
    long countAdmins();
    int insertInitialAdmin(AdminUser admin);
    Optional<AdminUser> findAdminByLoginId(@Param("loginId") String loginId);
    Optional<AdminUser> findAdminById(@Param("id") Long id);
    int insertAdminLog(@Param("adminId") Long adminId, @Param("actionType") String actionType,
            @Param("targetType") String targetType, @Param("targetId") String targetId,
            @Param("actionDetail") String actionDetail);

    long countTotalUsers();
    long countNewUsersToday();
    long countActiveUsers7Days();
    long countTotalInventory();
    long countApiCallsToday();
    List<RecentUser> findRecentUsers(@Param("limit") int limit);

    int insertApiUsageLog(ApiUsageLog log);
    LocalDateTime findCurrentDatabaseHour();
    List<ApiUsageAggregate> findHourlyApiUsage(@Param("hours") int hours);

    int insertBatchHistory(BatchJobHistory history);
    int completeBatchHistory(BatchJobHistory history);
    List<BatchJobHistory> findBatchHistories(@Param("limit") int limit);
    BatchJobHistory findLatestBatchHistory();

    int insertAdminPushHistory(@Param("adminId") Long adminId, @Param("title") String title,
            @Param("content") String content, @Param("sentCount") int sentCount);
    Long findLastInsertedId();
    int insertNoticeForAllUsers(@Param("title") String title, @Param("content") String content);
    List<NoticeHistory> findNoticeHistories(@Param("limit") int limit);
    List<AuditLog> findAuditLogs(@Param("limit") int limit);
}
