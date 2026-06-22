package com.ssafy.yumyum.global.security.oauth2.user.info;

import java.util.Map;

import com.ssafy.yumyum.global.security.oauth2.user.OAuth2Provider;

public interface OAuth2UserInfo {
    OAuth2Provider getProvider();
    String getAccessToken();
    Map<String, Object> getAttributes();
    String getId();
    String getEmail();
    String getName();
    String getFirstName();
    String getLastName();
    String getNickname();
    String getProfileImageUrl();
}
