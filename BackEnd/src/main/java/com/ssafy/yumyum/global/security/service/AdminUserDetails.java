package com.ssafy.yumyum.global.security.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ssafy.yumyum.domain.admin.entity.AdminRole;

import lombok.Getter;

@Getter
public class AdminUserDetails implements UserDetails {
    private final Long id;
    private final String loginId;
    private final AdminRole role;

    public AdminUserDetails(Long id, String loginId, AdminRole role) {
        this.id = id;
        this.loginId = loginId;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return loginId;
    }
}
