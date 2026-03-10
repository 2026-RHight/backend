package com.reverse.performance.internal.exception;

import com.reverse.core.exception.BadRequestException;

public class PerformanceActionNotAllowedException extends BadRequestException {
    private static final String CODE = "PERFORMANCE_ACTION_NOT_ALLOWED";

    public PerformanceActionNotAllowedException(String message) {
        super(message);
    }

    public String getCode() {
        return CODE;
    }
}
