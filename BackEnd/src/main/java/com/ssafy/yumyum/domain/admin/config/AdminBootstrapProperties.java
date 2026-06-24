package com.ssafy.yumyum.domain.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.ssafy.yumyum.domain.admin.entity.AdminRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "admin.bootstrap")
public class AdminBootstrapProperties {
    private boolean enabled;
    private String loginId;
    private String password;
    private AdminRole role = AdminRole.SUPER_ADMIN;
}
