package com.reverse.core.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final LocalDateTime timestamp;

    private final String message;

    @Builder
    public ErrorResponse(String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

    public static ErrorResponse of(Exception exception) {
        return ErrorResponse.builder().message(exception.getMessage()).build();
    }
}
