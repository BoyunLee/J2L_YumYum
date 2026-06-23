package com.ssafy.yumyum.global.security.oauth2.user.info;

import java.util.Map;

import com.ssafy.yumyum.global.security.oauth2.user.OAuth2Provider;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    private final String accessToken;
    private final String id;
    private final String email;
    private final String name;
    private final String firstName;
    private final String lastName;
    private final String nickName;
    private final String profileImageUrl;

    public KakaoOAuth2UserInfo(String accessToken, Map<String, Object> attributes) {
        this.accessToken = accessToken;

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
        this.attributes = kakaoProfile;

        this.id = ((Long) attributes.get("id")).toString();
        this.email = (String) kakaoAccount.get("email");
        this.name = null;
        this.firstName = null;
        this.lastName = null;
        this.nickName = (String) attributes.get("nickname");
        this.profileImageUrl = (String) attributes.get("profile_image_url");

        this.attributes.put("id", id);
        this.attributes.put("email", this.email);
    }

    @Override
    public OAuth2Provider getProvider() {
        return OAuth2Provider.KAKAO;
    }

    @Override
    public String getAccessToken() {
        return this.accessToken;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getEmail() {
        return this.email;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getFirstName() {
        return this.firstName;
    }

    @Override
    public String getLastName() {
        return this.lastName;
    }

    @Override
    public String getNickname() {
        return this.nickName;
    }

    @Override
    public String getProfileImageUrl() {
        return this.profileImageUrl;
    }
}
