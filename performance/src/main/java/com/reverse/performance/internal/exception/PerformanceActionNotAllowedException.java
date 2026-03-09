package com.reverse.performance.internal.exception;

import com.reverse.core.exception.BadRequestException;

public class PerformanceActionNotAllowedException extends BadRequestException {
    public PerformanceActionNotAllowedException(String message) {
        super(message);
    }
}
