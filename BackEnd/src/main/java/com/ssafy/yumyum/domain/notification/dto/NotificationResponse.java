package com.ssafy.yumyum.domain.notification.dto;

import java.time.LocalDateTime;

import com.ssafy.yumyum.domain.notification.entity.Notification;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        LocalDateTime createdAt,
        boolean read) {

    public static NotificationResponse from(Notification notification) {
        String type = "EXPIRED".equals(notification.getExpirationStatus()) ? "expired" : "expiry";
        return new NotificationResponse(
                notification.getId(), type, notification.getTitle(), notification.getContent(),
                notification.getCreatedAt(), notification.isRead());
    }
}
