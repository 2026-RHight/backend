package com.reverse.core.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    private final String code;

    public NotFoundException(String message) {
        super(message);
        this.code = "NOT_FOUND";
    }

    public NotFoundException(String code, String message) {
        super(message);
        this.code = (code == null || code.isBlank()) ? "NOT_FOUND" : code;
    }
}
