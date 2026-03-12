package com.reverse.hr.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminEmployeeRevealRequestDTO(
        @NotBlank(message = "조회 항목은 필수입니다.") String fieldType,
        @NotBlank(message = "조회 사유는 필수입니다.") String reason) {}
