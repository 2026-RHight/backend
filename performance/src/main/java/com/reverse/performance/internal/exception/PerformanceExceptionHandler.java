package com.reverse.performance.internal.exception;

import com.reverse.core.exception.ErrorResponse;
import com.reverse.core.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.reverse.performance")
public class PerformanceExceptionHandler {

    @ExceptionHandler(PerformanceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePerformanceNotFoundException(
            PerformanceNotFoundException ex) {
        log.error("PerformanceNotFoundException 발생: code={}, message={}", ex.getCode(), ex.getMessage());

        ErrorResponse error =
                ErrorResponse.builder().code(ex.getCode()).message(ex.getMessage()).build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(error));
    }

    @ExceptionHandler(PerformanceActionNotAllowedException.class)
    public ResponseEntity<ApiResponse<Void>> handlePerformanceActionNotAllowedException(
            PerformanceActionNotAllowedException ex) {
        log.error("PerformanceActionNotAllowedException 발생: message={}", ex.getMessage());

        ErrorResponse error =
                ErrorResponse.builder()
                        .code("PERFORMANCE_ACTION_NOT_ALLOWED")
                        .message(ex.getMessage())
                        .build();

        return ResponseEntity.badRequest().body(ApiResponse.fail(error));
    }
}
