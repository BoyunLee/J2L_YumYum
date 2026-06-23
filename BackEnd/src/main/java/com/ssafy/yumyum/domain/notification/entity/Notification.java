package com.ssafy.yumyum.domain.notification.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Notification {
    private Long id;
    private String expirationStatus;
    private String title;
    private String content;
    private boolean read;
    private LocalDateTime createdAt;
}
