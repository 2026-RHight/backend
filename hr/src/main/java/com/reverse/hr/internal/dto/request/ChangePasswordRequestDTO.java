package com.reverse.hr.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(
        @NotBlank
                @Size(min = 8, max = 15)
                @Pattern(
                        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,15}$",
                        message = "비밀번호는 8~15자, 영문/숫자/특수문자를 포함해야 합니다.")
                String newPassword,
        @NotBlank String confirmPassword) {}
