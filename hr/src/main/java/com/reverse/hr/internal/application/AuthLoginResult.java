package com.reverse.hr.internal.application;

import com.reverse.hr.internal.dto.response.LoginResponseDTO;

public record AuthLoginResult(LoginResponseDTO response, String refreshToken) {}
