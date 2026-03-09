package com.reverse.approval.internal.exception;

import com.reverse.core.exception.NotFoundException;

public class AttachmentNotFoundException extends NotFoundException {
    public AttachmentNotFoundException(String message) {
        super(message);
    }
}
