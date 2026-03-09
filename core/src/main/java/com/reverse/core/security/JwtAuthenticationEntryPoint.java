package com.reverse.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reverse.core.exception.ErrorResponse;
import com.reverse.core.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 인증 실패 시 401 응답을 반환하는 핸들러
 *
 * <p>토큰이 없거나, 만료되었거나, 위조된 경우 호출된다.
 *
 * @author sekong11
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse error = ErrorResponse.builder().message("인증이 필요합니다.").build();

        ApiResponse<Void> body = ApiResponse.fail(error);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
