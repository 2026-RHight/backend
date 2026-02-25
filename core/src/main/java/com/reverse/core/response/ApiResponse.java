package com.reverse.core.response;

import com.reverse.rhight.global.exception.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorResponse error;

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(Exception exception) {
        return new ApiResponse<>(false, null, ErrorResponse.of(exception));
    }

    public static <T> ApiResponse<T> fail(ErrorResponse error) {
        return new ApiResponse<>(false, null, error);
    }
}
