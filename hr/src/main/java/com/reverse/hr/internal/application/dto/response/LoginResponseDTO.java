package com.reverse.hr.internal.application.dto.response;

public record LoginResponseDTO(
        boolean requiresPasswordChange,
        String accessToken,
        String passwordChangeTicket
) {
    public LoginResponseDTO {
        if (requiresPasswordChange && (passwordChangeTicket == null || accessToken != null || passwordChangeTicket.isBlank())){
            throw new IllegalArgumentException("비밀번호 변경 필요 시 ticket만 내려야 합니다.");
        }
        if (!requiresPasswordChange && (accessToken == null || passwordChangeTicket != null || accessToken.isBlank())) {
            throw new IllegalArgumentException("일반 로그인 성공 시 accessToken만 내려야 합니다.");
        }
    }
}
