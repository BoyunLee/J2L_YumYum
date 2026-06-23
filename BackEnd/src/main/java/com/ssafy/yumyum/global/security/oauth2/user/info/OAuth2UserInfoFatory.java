package com.ssafy.yumyum.global.security.oauth2.user.info;

import java.util.Map;

import com.ssafy.yumyum.global.security.oauth2.exception.OAuth2AuthenticationProcessingException;
import com.ssafy.yumyum.global.security.oauth2.user.OAuth2Provider;

public class OAuth2UserInfoFatory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, String accessToken, Map<String, Object> attributes) {
        if (OAuth2Provider.GOOGLE.getProvider().equals(registrationId)) {
            return new GoogleOAuth2UserInfo(accessToken, attributes);
        } else if (OAuth2Provider.NAVER.getProvider().equals(registrationId)) {
            return new NaverOAuth2UserInfo(accessToken, attributes);
        } else if (OAuth2Provider.KAKAO.getProvider().equals(registrationId)) {
            return new KakaoOAuth2UserInfo(accessToken, attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException("Login with " + registrationId + " is not supported");
        }
    }
}
