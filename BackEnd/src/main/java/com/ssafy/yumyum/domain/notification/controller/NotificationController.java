package com.ssafy.yumyum.domain.notification.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.domain.notification.dto.NotificationResponse;
import com.ssafy.yumyum.domain.notification.service.NotificationService;
import com.ssafy.yumyum.global.response.ResponseBody;
import com.ssafy.yumyum.global.response.ResponseUtil;
import com.ssafy.yumyum.global.security.service.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseBody<List<NotificationResponse>> findAll(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseUtil.createSuccessResponse(notificationService.findAll(user.getId()));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseBody<Void> markAsRead(@AuthenticationPrincipal CustomUserDetails user,
                                         @PathVariable Long notificationId) {
        notificationService.markAsRead(user.getId(), notificationId);
        return ResponseUtil.createSuccessResponse();
    }

    @PutMapping("/read-all")
    public ResponseBody<Void> markAllAsRead(@AuthenticationPrincipal CustomUserDetails user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseUtil.createSuccessResponse();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseBody<Void> delete(@AuthenticationPrincipal CustomUserDetails user,
                                     @PathVariable Long notificationId) {
        notificationService.delete(user.getId(), notificationId);
        return ResponseUtil.createSuccessResponse();
    }
}
