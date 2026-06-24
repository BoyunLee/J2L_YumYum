package com.ssafy.yumyum.domain.admin.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.ssafy.yumyum.domain.admin.dao.AdminDao;
import com.ssafy.yumyum.domain.admin.entity.BatchJobHistory;
import com.ssafy.yumyum.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchExecutionService {
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final String JOB_NAME = "EXPIRATION_NOTIFICATION";

    private final AdminDao adminDao;
    private final NotificationService notificationService;

    public BatchJobHistory runExpirationNotificationBatch(String triggerType) {
        BatchJobHistory history = new BatchJobHistory();
        history.setJobName(JOB_NAME);
        history.setTriggerType(triggerType);
        history.setStatus("RUNNING");
        history.setStartedAt(LocalDateTime.now(SEOUL_ZONE));
        adminDao.insertBatchHistory(history);

        try {
            int processed = notificationService.createExpirationNotifications(LocalDate.now(SEOUL_ZONE));
            finish(history, "SUCCESS", processed, null);
            return history;
        } catch (RuntimeException exception) {
            finish(history, "FAILED", 0, exception.getClass().getSimpleName());
            throw exception;
        }
    }

    private void finish(BatchJobHistory history, String status, int processedCount, String errorMessage) {
        LocalDateTime finishedAt = LocalDateTime.now(SEOUL_ZONE);
        history.setStatus(status);
        history.setProcessedCount(processedCount);
        history.setFinishedAt(finishedAt);
        history.setDurationMs(Duration.between(history.getStartedAt(), finishedAt).toMillis());
        history.setErrorMessage(errorMessage);
        adminDao.completeBatchHistory(history);
    }
}
