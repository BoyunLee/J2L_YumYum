package com.ssafy.yumyum.domain.notification.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.yumyum.domain.notification.dao.NotificationDao;
import com.ssafy.yumyum.domain.notification.dto.NotificationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationDao notificationDao;

    @Transactional
    public int createExpirationNotifications(LocalDate referenceDate) {
        return notificationDao.insertExpirationNotifications(referenceDate);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findAll(Long userId) {
        return notificationDao.findAllByUserId(userId).stream().map(NotificationResponse::from).toList();
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        notificationDao.markAsRead(notificationId, userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationDao.markAllAsRead(userId);
    }

    @Transactional
    public void delete(Long userId, Long notificationId) {
        notificationDao.delete(notificationId, userId);
    }
}
