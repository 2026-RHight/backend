package com.reverse.hr.internal.application.dto.response;

public record LoginResponseDTO(
        boolean requiresPasswordChange,
        String accessToken,
        String passwordChangeTicket
) {
}
