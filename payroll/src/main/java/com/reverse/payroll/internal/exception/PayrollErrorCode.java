package com.reverse.payroll.internal.exception;

import org.springframework.http.HttpStatus;

public enum PayrollErrorCode {
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "PAYROLL-001", "비밀번호가 일치하지 않습니다."),
    PAYROLL_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYROLL-002", "급여 명세서를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    PayrollErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
