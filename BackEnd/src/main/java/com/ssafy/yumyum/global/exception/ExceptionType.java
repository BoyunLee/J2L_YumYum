package com.ssafy.yumyum.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ExceptionType {
    
    // common
    UNEXPECTED_SERVER_ERROR(INTERNAL_SERVER_ERROR, "C001", "예상치 못한 에러 발생"),
    BINDING_ERROR(BAD_REQUEST, "C002", "바인딩시 에러 발생"),
    ESSENTIAL_FIELD_MISSING_ERROR(BAD_REQUEST, "C003", "필수적인 필드 부재"),
    INVALID_JSON_FORMAT(BAD_REQUEST, "C004", "잘못된 JSON 데이터 형식"),
    NOT_SUPPORTED_METHOD(METHOD_NOT_ALLOWED, "C005", "허용되지 않은 http method 접근"),
    LOCK_ACQUIRE_FAILED(CONFLICT, "C006", "락 획득 실패"),
    CONCURRENCY_CONFLICT(CONFLICT, "C007", "동시에 요청이 처리되어 충돌이 발생"),

    // user
    USER_ALREADY_EXIST(HttpStatus.CONFLICT, "U001", "이미 존재하는 회원입니다."),
    USER_NOT_FOUND(NOT_FOUND, "U002", "존재하지 않는 유저입니다."),
    NOT_CORRECT_PASSWORD(UNAUTHORIZED, "U003", "기존 비밀번호가 일치하지 않습니다."),
    INVALID_EMAIL_FORMAT(BAD_REQUEST, "U004", "이메일 형식이 올바르지 않습니다."),
    EMAIL_SEND_ERROR(INTERNAL_SERVER_ERROR, "U005", "알수없는 오류로 인해 이메일 전송에 실패하였습니다."),
    INVALID_EMAIL_CODE(UNAUTHORIZED, "U006", "인증번호가 일치하지 않습니다."),
    EXPIRED_EMAIL_CODE(BAD_REQUEST, "U007", "만료된 인증번호입니다."),
    EMAIL_NOT_VERIFIED(UNAUTHORIZED, "U008", "이메일 인증이 완료되지 않았습니다."),

    USER_PROFILE_ALREADY_COMPLETED(CONFLICT, "U009", "이미 추가 정보 입력을 완료한 사용자입니다."),

    // auth
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "A001", "아이디 또는 비밀번호가 일치하지 않습니다."),
    GENERATE_TOKEN_ERROR(INTERNAL_SERVER_ERROR, "A002", "토큰 생성 과정 중 오류가 발생했습니다."),
    FIND_PASSWORD_ERROR(BAD_REQUEST, "A003", "웹메일 또는 아이디, 비밀번호가 일치하지 않습니다."),
    TEMPORARY_PASSWORD_ERROR(INTERNAL_SERVER_ERROR,  "A004", "임시 비밀번호 생성 오류"),
    INVALID_ACCESS_TOKEN(UNAUTHORIZED, "A005", "Access Token 이 유효하지 않습니다."),
    NOT_FOUND_ACCESS_TOKEN(UNAUTHORIZED, "A006", "Access Token 이 존재하지 않습니다."),
    EXPIRED_ACCESS_TOKEN(UNAUTHORIZED, "A007", "Access Token 만료"),
    INVALID_REFRESH_TOKEN(UNAUTHORIZED, "A008", "Refresh Token 이 유효하지 않습니다."),
    NOT_FOUND_REFRESH_TOKEN(UNAUTHORIZED, "A009", "Refresh Token 이 존재하지 않습니다."),
    EXPIRED_REFRESH_TOKEN(UNAUTHORIZED, "A010", "Refresh Token Token 만료"),
    AUTHORIZATION_DENIED(FORBIDDEN, "A011", "권한이 없습니다."),

    // refrigerator
    REFRIGERATOR_ITEM_NOT_FOUND(NOT_FOUND, "R001", "존재하지 않는 냉장고 재고입니다."),

    // file


    // 

    ;
    
    private final HttpStatus status;
    private final String code;
    private final String message;
}
