package com.ssafy.yumyum.domain.notification.dao;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.yumyum.domain.notification.entity.Notification;

@Mapper
public interface NotificationDao {
    int insertExpirationNotifications(@Param("referenceDate") LocalDate referenceDate);
    List<Notification> findAllByUserId(@Param("userId") Long userId);
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);
    int markAllAsRead(@Param("userId") Long userId);
    int delete(@Param("id") Long id, @Param("userId") Long userId);
}
