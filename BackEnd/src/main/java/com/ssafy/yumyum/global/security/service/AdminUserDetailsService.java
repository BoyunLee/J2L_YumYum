package com.ssafy.yumyum.global.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.ssafy.yumyum.domain.admin.dao.AdminDao;
import com.ssafy.yumyum.domain.admin.entity.AdminUser;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {
    private final AdminDao adminDao;

    @Override
    public UserDetails loadUserByUsername(String adminId) throws UsernameNotFoundException {
        AdminUser admin = adminDao.findAdminById(Long.valueOf(adminId))
                .orElseThrow(() -> new BusinessException(ExceptionType.INVALID_LOGIN));
        return new AdminUserDetails(admin.getId(), admin.getLoginId(), admin.getRole());
    }
}
