package com.ssafy.yumyum.domain.admin.config;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ssafy.yumyum.domain.admin.dao.AdminDao;
import com.ssafy.yumyum.domain.admin.entity.AdminUser;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer {
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final AdminDao adminDao;
    private final PasswordEncoder passwordEncoder;
    private final AdminBootstrapProperties properties;

    @PostConstruct
    public void init() {
        if (!properties.isEnabled()) {
            return;
        }

        validateProperties();
        if (adminDao.countAdmins() > 0) {
            log.info("Admin bootstrap skipped because an administrator already exists.");
            return;
        }

        AdminUser admin = new AdminUser();
        admin.setLoginId(properties.getLoginId().trim());
        admin.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
        admin.setRole(properties.getRole());

        int inserted = adminDao.insertInitialAdmin(admin);
        if (inserted == 1) {
            log.info("Initial administrator created: loginId={}, role={}", admin.getLoginId(), admin.getRole());
        } else {
            log.info("Admin bootstrap skipped because another instance created the administrator first.");
        }
    }

    private void validateProperties() {
        if (properties.getLoginId() == null || properties.getLoginId().isBlank()) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_LOGIN_ID must be configured when admin bootstrap is enabled.");
        }
        if (properties.getLoginId().trim().length() > 100) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_LOGIN_ID must be 100 characters or fewer.");
        }
        if (properties.getPassword() == null || properties.getPassword().isBlank()) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_PASSWORD must be configured when admin bootstrap is enabled.");
        }
        if (properties.getPassword().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_PASSWORD must be 72 bytes or fewer for BCrypt.");
        }
    }
}
