package com.ssafy.yumyum.domain.admin.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchJobHistory {
    private Long id;
    private String jobName;
    private String triggerType;
    private String status;
    private Integer processedCount;
    private Long durationMs;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String errorMessage;
}
