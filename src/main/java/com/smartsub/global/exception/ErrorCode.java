package com.smartsub.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."),

    // Store
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE_NOT_FOUND", "매장을 찾을 수 없습니다."),
    STORE_ALREADY_EXISTS(HttpStatus.CONFLICT, "STORE_ALREADY_EXISTS", "이미 매장을 보유하고 있습니다. 현재 1인 1매장만 지원됩니다."),

    // Tenant
    INVALID_TENANT(HttpStatus.BAD_REQUEST, "INVALID_TENANT", "유효하지 않은 테넌트입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 올바르지 않습니다."),

    // Report
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "리포트를 찾을 수 없습니다."),

    // Chat
    CHAT_SERVICE_BUSY(HttpStatus.TOO_MANY_REQUESTS, "CHAT_SERVICE_BUSY", "일시적으로 요청이 몰려 있습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}