package com.ssafy.yumyum.domain.admin.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUser {
    private Long id;
    private String loginId;
    private String passwordHash;
    private AdminRole role;
    private LocalDateTime createdAt;
}
