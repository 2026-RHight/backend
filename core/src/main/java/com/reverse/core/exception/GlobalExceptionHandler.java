package com.reverse.core.exception;


import com.reverse.core.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException ex) {
        log.error("UnauthorizedException 발생: code={}, message={}", ex.getCode(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(error));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbiddenException(ForbiddenException ex) {
        log.error("ForbiddenException 발생:  code={}, message: {}", ex.getCode(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(error));
    }

    // JSON 파싱/바인딩 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        ErrorResponse error = ErrorResponse.builder().message("요청 본문(JSON) 형식이 올바르지 않습니다.").build();

        return ResponseEntity.badRequest().body(ApiResponse.fail(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("예상치 못한 에러 발생: ", ex);

        ErrorResponse error = ErrorResponse.builder().message("서버 내부 오류가 발생했습니다.").build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        String defaultMessage = fieldError.getDefaultMessage();
                        return (defaultMessage == null || defaultMessage.isBlank())
                                ? "요청 값이 올바르지 않습니다."
                                : defaultMessage;
                    }
                    return "요청 값이 올바르지 않습니다.";
                })
                .orElse("요청 값이 올바르지 않습니다.");

        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_REQUEST")
                .message(message)
                .build();

        return ResponseEntity.badRequest().body(ApiResponse.fail(error));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestPart(MissingServletRequestPartException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_REQUEST")
                .message("필수 요청 파트가 누락되었습니다: " + ex.getRequestPartName())
                .build();

        return ResponseEntity.badRequest().body(ApiResponse.fail(error));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .code("FILE_TOO_LARGE")
                .message("업로드 파일 용량이 제한을 초과했습니다. 최대 50MB까지 업로드할 수 있습니다.")
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ApiResponse.fail(error));
    }
}
