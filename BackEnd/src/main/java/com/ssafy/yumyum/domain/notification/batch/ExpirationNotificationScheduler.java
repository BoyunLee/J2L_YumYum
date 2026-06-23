package com.ssafy.yumyum.domain.notification.batch;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ssafy.yumyum.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpirationNotificationScheduler {
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private final NotificationService notificationService;

    // TODO: 테스트 종료 후 notification.expiration.cron을 매일 오전 7시(0 0 7 * * *)로 변경하세요.
    @Scheduled(cron = "${notification.expiration.cron:0 * * * * *}", zone = "Asia/Seoul")
    public void createExpirationNotifications() {
        LocalDate referenceDate = LocalDate.now(SEOUL_ZONE);
        int createdCount = notificationService.createExpirationNotifications(referenceDate);
        log.info("Expiration notification batch completed: referenceDate={}, createdCount={}",
                referenceDate, createdCount);
    }
}
