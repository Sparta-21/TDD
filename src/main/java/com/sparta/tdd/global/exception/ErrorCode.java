package com.sparta.tdd.global.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "요청이 현재 서버 상태와 충돌합니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 id 입니다."),

    // AUTH 도메인 관련
    ACCESS_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "액세스 토큰이 존재하지 않습니다."),
    ACCESS_TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "금지된 액세스 토큰입니다."),
    ACCESS_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "금지된 리프레시 토큰입니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    INVALID_LOGIN_CREDENTIALS(HttpStatus.UNAUTHORIZED, "올바르지 않은 요청입니다."),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 username 입니다."),

    // USER 도메인 관련
    NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용중인 닉네임입니다."),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "이미 사용중인 비밀번호입니다."),
    ALREADY_MANAGER(HttpStatus.BAD_REQUEST, "이미 매니저 권한입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    CANNOT_MODIFY_OTHER_MEMBER(HttpStatus.FORBIDDEN, "다른 사용자의 정보를 수정할 수 없습니다."),

    // STORE 도메인 관련
    STORE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "상점 관련 작업을 수행할 권한이 없습니다."),
    STORE_OWNERSHIP_DENIED(HttpStatus.FORBIDDEN, "본인의 상점만 수정/삭제할 수 있습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상점입니다."),

    // ORDER 도메인 관련
    ORDER_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    MENU_NOT_IN_STORE(HttpStatus.BAD_REQUEST, "해당 가게의 메뉴가 아닙니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메뉴가 포함되어 있습니다."),
    MENU_INVALID_INFO(HttpStatus.BAD_REQUEST, "메뉴 정보가 올바르지 않습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),

    // MENU 도메인 관련

    // REVIEW 도메인 관련
    REVIEW_NOT_OWNED(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정/삭제할 수 있습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰입니다."),
    REVIEW_REPLY_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 답글이 존재합니다."),
    REVIEW_REPLY_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "해당 가게의 소유자만 답글을 작성할 수 있습니다."),
    REVIEW_REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 답글입니다."),

    // PAYMENT 도메인 관련
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 결제 내역입니다."),
    INVALID_CARD_COMPANY(HttpStatus.BAD_REQUEST, "유효하지 않은 카드사입니다."),
    GET_STORE_PAYMENT_DENIED(HttpStatus.FORBIDDEN, "본인의 상점의 결제 내역만 조회할 수 있습니다."),

    // AI 도메인 관련
    ;

    private final HttpStatus status;
    private final String message;
}
