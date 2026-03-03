package com.reverse.hr.internal.exception;

public final class AuthErrorCode {
    private AuthErrorCode() {}

    public static final String AUTH_LOGIN_FAILED = "AUTH_LOGIN_FAILED";
    public static final String PASSWORD_RESET_REQUIRED = "PASSWORD_RESET_REQUIRED";
    public static final String INVALID_PASSWORD_CONFIRM = "INVALID_PASSWORD_CONFIRM";
    public static final String INVALID_NEW_PASSWORD =  "INVALID_NEW_PASSWORD";
}
