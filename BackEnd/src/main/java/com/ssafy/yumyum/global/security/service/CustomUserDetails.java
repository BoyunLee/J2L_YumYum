package com.ssafy.yumyum.global.security.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ssafy.yumyum.domain.auth.UserRole;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String name;
    private final UserRole role;

    public CustomUserDetails(Long id, String name, UserRole role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public String getPassword() {
        throw new UnsupportedOperationException("Password is not stored in JWT principal");
    }

    @Override
    public String getUsername() {
        return name;
    }
}
