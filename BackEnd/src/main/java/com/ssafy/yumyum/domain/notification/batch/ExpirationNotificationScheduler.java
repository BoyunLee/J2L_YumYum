package com.ssafy.yumyum.domain.notification.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ssafy.yumyum.domain.admin.entity.BatchJobHistory;
import com.ssafy.yumyum.domain.admin.service.BatchExecutionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpirationNotificationScheduler {
    private final BatchExecutionService batchExecutionService;

    @Scheduled(cron = "${notification.expiration.cron:0 0 7 * * *}", zone = "Asia/Seoul")
    public void createExpirationNotifications() {
        BatchJobHistory history = batchExecutionService.runExpirationNotificationBatch("SCHEDULED");
        log.info("Expiration notification batch completed: historyId={}, createdCount={}, durationMs={}",
                history.getId(), history.getProcessedCount(), history.getDurationMs());
    }
}
