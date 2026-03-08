package com.reverse.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reverse.core.exception.ErrorResponse;
import com.reverse.core.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 권한 부족 시 403 응답을 반환하는 핸들러
 *
 * <p>인증은 됐지만 해당 API에 접근 권한이 없는 경우 호출된다.
 *
 * @author sekong11
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse error = ErrorResponse.builder().message("접근 권한이 없습니다.").build();

        ApiResponse<Void> body = ApiResponse.fail(error);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
