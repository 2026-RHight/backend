package com.reverse.approval.internal.exception;

import com.reverse.core.exception.NotFoundException;

public class ApprovalNotFoundException extends NotFoundException {
    public ApprovalNotFoundException(String message) {
        super(message);
    }
}
