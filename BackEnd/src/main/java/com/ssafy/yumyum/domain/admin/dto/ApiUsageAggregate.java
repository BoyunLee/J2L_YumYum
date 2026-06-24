package com.ssafy.yumyum.domain.admin.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiUsageAggregate {
    private LocalDateTime hour;
    private String apiType;
    private Long callCount;
    private Long successCount;
}
