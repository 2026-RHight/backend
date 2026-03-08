package com.reverse.hr.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeMyPasswordRequestDTO(
        @NotBlank String currentPassword,
        @NotBlank String newPassword,
        @NotBlank String confirmPassword) {}
