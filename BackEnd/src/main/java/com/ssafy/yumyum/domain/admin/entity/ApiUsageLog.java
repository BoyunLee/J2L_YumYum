package com.ssafy.yumyum.domain.admin.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiUsageLog {
    private Long id;
    private Long userId;
    private String apiType;
    private boolean success;
    private Long durationMs;
    private String errorCode;
    private LocalDateTime createdAt;
}
