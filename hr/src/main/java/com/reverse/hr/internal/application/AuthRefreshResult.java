package com.reverse.hr.internal.application;

import com.reverse.hr.internal.dto.response.RefreshTokenResponseDTO;

public record AuthRefreshResult(RefreshTokenResponseDTO response, String refreshToken) {}
