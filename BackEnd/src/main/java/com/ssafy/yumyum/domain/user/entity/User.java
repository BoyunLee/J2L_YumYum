package com.ssafy.yumyum.domain.user.entity;

import java.util.Date;

import com.ssafy.yumyum.domain.auth.UserRole;
import com.ssafy.yumyum.global.security.oauth2.user.OAuth2Provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
    private OAuth2Provider provider;
    private String providerUserId;
    private String profileImageUrl;
    private UserRole role;

    private Gender gender;
    private Date birth_date;
    private int height;
    private int weight;
    private ActivityLevel activityLevel;
}

