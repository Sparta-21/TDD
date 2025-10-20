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
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),
    ORDER_CANCELLATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "주문 생성 후 5분이 지나 취소할 수 없습니다."),

    // MENU 도메인 관련
    IS_HIDDEN_MENU(HttpStatus.BAD_REQUEST, "숨겨진 메뉴입니다."),
    MENU_NOT_IN_STORE(HttpStatus.BAD_REQUEST, "해당 가게의 메뉴가 아닙니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메뉴가 포함되어 있습니다."),
    MENU_INVALID_INFO(HttpStatus.BAD_REQUEST, "메뉴 정보가 올바르지 않습니다."),
    MENU_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "해당 메뉴에 대한 권한이 없습니다."),

    // REVIEW 도메인 관련
    REVIEW_NOT_OWNED(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정/삭제할 수 있습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰입니다."),
    REVIEW_REPLY_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 답글이 존재합니다."),
    REVIEW_REPLY_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "해당 가게의 소유자만 답글을 작성할 수 있습니다."),
    REVIEW_REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 답글입니다."),
    DUPLICATE_REVIEW(HttpStatus.CONFLICT, "이미 해당 주문에 대한 리뷰가 존재합니다."),

    // PAYMENT 도메인 관련
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 결제 내역입니다."),
    INVALID_CARD_COMPANY(HttpStatus.BAD_REQUEST, "유효하지 않은 카드사입니다."),
    GET_STORE_PAYMENT_DENIED(HttpStatus.FORBIDDEN, "본인의 상점의 결제 내역만 조회할 수 있습니다."),
    PAYMENT_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "주문에 대한 결제건이 이미 존재합니다."),
    INVALID_PAYMENT_REQUEST(HttpStatus.BAD_REQUEST, "올바른 주문 요청이 아닙니다."),
    POINT_PROCESSING_FAILED(HttpStatus.BAD_REQUEST, "포인트 적립 관련 오류입니다."),
    PAYMENT_CANCEL_TIME_EXPIRED(HttpStatus.CONFLICT, "결제 후 5분이 지나 취소할 수 없습니다."),

    // AI 도메인 관련

    // Cart 도메인 관련
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 아이템을 찾을 수 없습니다."),
    CART_ITEM_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "수량은 1개 이상이어야 합니다."),
    CART_ITEM_NOT_OWNED(HttpStatus.FORBIDDEN, "본인의 장바구니 아이템만 수정할 수 있습니다."),
    CART_DIFFERENT_STORE(HttpStatus.BAD_REQUEST,
        "장바구니에는 한 가게의 메뉴만 담을 수 있습니다. 기존 장바구니를 비우고 다시 시도해주세요."),

    // COUPON 도메인 관련
    COUPON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Scope 설정이 잘못되었습니다."),
    COUPON_ALREADY_ISSUED(HttpStatus.BAD_REQUEST, "이미 사용자가 발급하여 수정할 수 없습니다."),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 쿠폰입니다."),
    COUPON_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    COUPON_ALL_SOLD_OUT(HttpStatus.BAD_REQUEST, "쿠폰이 모두 소진되었습니다."),
    USER_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저쿠폰입니다."),
    COUPON_MIN_PRICE_INVALID(HttpStatus.BAD_REQUEST, "최소금액이 부족합니다."),

    // ADDRESS 도메인 관련
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "주소를 찾을 수 없습니다."),
    ADDRESS_STORE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "해당 가게의 소유자만 주소를 수정할 수 있습니다."),
    ADDRESS_USER_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "본인의 주소만 수정할 수 있습니다.");

    private final HttpStatus status;
    private final String message;
}
