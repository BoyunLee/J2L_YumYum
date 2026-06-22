package com.ssafy.yumyum.global.security.oauth2.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ssafy.yumyum.global.security.oauth2.user.info.OAuth2UserInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class OAuth2UserPrincipal implements OAuth2User, UserDetails {

    private final OAuth2UserInfo userInfo;

    public OAuth2UserPrincipal(OAuth2UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userInfo.getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired(); // true
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();  // true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired(); // true
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();   // true
    }

    @Override
    public Map<String, Object> getAttributes() {
        return userInfo.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return userInfo.getId();
    }

    public OAuth2UserInfo getUserInfo() {
        return this.userInfo;
    }

}
