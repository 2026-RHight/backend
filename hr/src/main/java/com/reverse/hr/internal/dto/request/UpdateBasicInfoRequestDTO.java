package com.reverse.hr.internal.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBasicInfoRequestDTO(
        @Email
        @NotBlank
        @Size(max = 100)
        String email,
        @NotBlank
        @Size(max = 50)
        String phone,
        @NotBlank
        @Size(max = 255)
        String address
) {
}
