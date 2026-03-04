package com.reverse.hr.internal.application.dto.response;

/**
 * 로그인 API 응답 DTO.
 * 초기 비밀번호 상태일 때는 비밀번호 변경 티켓만 반환하고,
 * 일반 로그인일 때는 액세스 토큰과 사용자 프로필을 반환한다.
 */
public record LoginResponseDTO(
        boolean requiresPasswordChange,
        String accessToken,
        String passwordChangeTicket,
        LoginUserProfileDTO userProfile
) {
    public LoginResponseDTO {
        if (requiresPasswordChange && (passwordChangeTicket == null || accessToken != null || passwordChangeTicket.isBlank() || userProfile != null)){
            throw new IllegalArgumentException("비밀번호 변경 필요 시 ticket만 내려야 합니다.");
        }
        if (!requiresPasswordChange && (accessToken == null || passwordChangeTicket != null || accessToken.isBlank() || userProfile == null)) {
            throw new IllegalArgumentException("일반 로그인 성공 시 accessToken + userProfile만 내려야 합니다.");
        }
    }
}
