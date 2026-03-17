package com.reverse.hr.internal.web;

import com.reverse.core.exception.UnauthorizedException;
import com.reverse.core.response.ApiResponse;
import com.reverse.hr.internal.application.AuthLoginResult;
import com.reverse.hr.internal.application.AuthRefreshResult;
import com.reverse.hr.internal.application.AuthService;
import com.reverse.hr.internal.dto.request.ChangePasswordRequestDTO;
import com.reverse.hr.internal.dto.request.InitializeRequestDTO;
import com.reverse.hr.internal.dto.request.LoginRequestDTO;
import com.reverse.hr.internal.dto.response.LoginResponseDTO;
import com.reverse.hr.internal.dto.response.RefreshTokenResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final AuthService authService;

    @Value("${security.cookie.secure:false}")
    private boolean secureCookie;

    @Value("${security.cookie.same-site:Lax}")
    private String sameSite;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMillis;

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponse<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request, HttpServletResponse response) {
        AuthLoginResult result = authService.login(request);
        writeRefreshTokenCookie(response, result.refreshToken());
        return ApiResponse.success(result.response());
    }

    @Operation(summary = "초기화된 비밀번호 변경")
    @PatchMapping("/password")
    public ApiResponse<LoginResponseDTO> changeInitialPassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ChangePasswordRequestDTO request,
            HttpServletResponse response) {
        String ticket = extractBearerToken(authorization);
        AuthLoginResult result = authService.changeInitialPassword(ticket, request);
        writeRefreshTokenCookie(response, result.refreshToken());
        return ApiResponse.success(result.response());
    }

    @Operation(summary = "비밀번호 초기화")
    @PatchMapping("/initialize/password")
    public ApiResponse<Void> initializePassword(@Valid @RequestBody InitializeRequestDTO dto) {
        authService.initializePassword(dto);
        return ApiResponse.success();
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(authorization, refreshToken);
        clearRefreshTokenCookie(response);
        return ApiResponse.success();
    }

    @Operation(summary = "액세스 토큰 재발급")
    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponseDTO> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        AuthRefreshResult result = authService.refresh(refreshToken);
        writeRefreshTokenCookie(response, result.refreshToken());
        return ApiResponse.success(result.response());
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("INVALID_TICKET", "비밀번호 변경 티켓이 필요합니다.");
        }
        String ticket = authorization.substring(7).trim();
        if (ticket.isEmpty()) {
            throw new UnauthorizedException("INVALID_TICKET", "비밀번호 변경 티켓이 필요합니다.");
        }
        return ticket;
    }

    private void writeRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            clearRefreshTokenCookie(response);
            return;
        }
        ResponseCookie cookie =
                ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                        .httpOnly(true)
                        .secure(secureCookie)
                        .sameSite(sameSite)
                        .path("/api/v1/auth")
                        .maxAge(refreshExpirationMillis / 1000)
                        .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie =
                ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                        .httpOnly(true)
                        .secure(secureCookie)
                        .sameSite(sameSite)
                        .path("/api/v1/auth")
                        .maxAge(0)
                        .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
