package com.reverse.performance.internal.exception;

import com.reverse.core.exception.NotFoundException;

public class PerformanceNotFoundException extends NotFoundException {
    public PerformanceNotFoundException(String message) {
        super(message);
    }

    public PerformanceNotFoundException(String code, String message) {
        super(code, message);
    }
}
