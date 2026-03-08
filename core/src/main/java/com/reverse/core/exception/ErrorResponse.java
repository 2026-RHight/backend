package com.reverse.core.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final String message;
    private final String code;

    @Builder
    public ErrorResponse(String code, String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.code = code;
    }

    public static ErrorResponse of(Exception exception) {
        if (exception instanceof UnauthorizedException ex) {
            return ErrorResponse.builder()
                    .code(ex.getCode())
                    .message(ex.getMessage())
                    .build();
        }

        if (exception instanceof ForbiddenException ex) {
            return ErrorResponse.builder()
                    .code(ex.getCode())
                    .message(ex.getMessage())
                    .build();
        }

        if (exception instanceof NotFoundException ex) {
            return ErrorResponse.builder()
                    .code(ex.getCode())
                    .message(ex.getMessage())
                    .build();
        }

        return ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("서버 내부 오류가 발생했습니다.")
                .build();
    }
}
