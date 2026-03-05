package com.reverse.hr.internal.web;

import com.reverse.core.exception.UnauthorizedException;
import com.reverse.core.response.ApiResponse;
import com.reverse.hr.internal.application.AuthService;
import com.reverse.hr.internal.dto.request.ChangePasswordRequestDTO;
import com.reverse.hr.internal.dto.request.InitializeRequestDTO;
import com.reverse.hr.internal.dto.request.LoginRequestDTO;
import com.reverse.hr.internal.dto.response.LoginResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request){
        LoginResponseDTO token = authService.login(request);
        return ApiResponse.success(token);
    }

    @PatchMapping("/password")
    public ApiResponse<LoginResponseDTO> changeInitialPassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ChangePasswordRequestDTO request)
    {
        String ticket = extractBearerToken(authorization);
        LoginResponseDTO token = authService.changeInitialPassword(ticket, request);
        return ApiResponse.success(token);
    }

    @PatchMapping("/initialize/password")
    public ApiResponse<Void> initializePassword(@Valid @RequestBody InitializeRequestDTO dto){
        authService.initializePassword(dto);
        return ApiResponse.success();
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
}
