package com.reverse.hr.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record InitializeRequestDTO(
        @NotBlank(message = "사번을 입력해주세요") String employeeNum,
        @NotBlank(message = "주민등록번호를 입력해주세요") String residentNum) {}
