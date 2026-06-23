package com.ssafy.yumyum.global.security.oauth2.user.info;

import java.util.Map;

import com.ssafy.yumyum.global.security.oauth2.user.OAuth2Provider;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    private final String accessToken;
    private final String id;
    private final String email;
    private final String name;
    private final String firstName;
    private final String lastName;
    private final String nickName;
    private final String profileImageUrl;

    public GoogleOAuth2UserInfo(String accessToken, Map<String, Object> attributes) {
        this.accessToken = accessToken;
        this.attributes = attributes;
        this.id = (String) attributes.get("sub");
        this.email = (String) attributes.get("email");
        this.name = (String) attributes.get("name");
        this.firstName = (String) attributes.get("given_name");
        this.lastName = (String) attributes.get("family_name");
        this.nickName = null;
        this.profileImageUrl = (String) attributes.get("picture");
    }

    @Override
    public OAuth2Provider getProvider() {
        return OAuth2Provider.GOOGLE;
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
