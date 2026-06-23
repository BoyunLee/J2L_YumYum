package com.ssafy.yumyum.domain.user.entity;

public enum UserRole {
    GUEST("회원 정보 미입력"),
    USER("사용자"),
    ADMIN("관리자");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
