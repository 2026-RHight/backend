package com.reverse.approval.internal.exception;

import com.reverse.core.exception.BadRequestException;

public class FormRequiredException extends BadRequestException {
    public FormRequiredException(String message) {
        super(message);
    }
}
