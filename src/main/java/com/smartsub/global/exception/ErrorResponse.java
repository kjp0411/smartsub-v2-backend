package com.smartsub.global.exception;

public record ErrorResponse(
    int status,
    String code,
    String message
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
            errorCode.getHttpStatus().value(),
            errorCode.getCode(),
            errorCode.getMessage()
        );
    }
}
