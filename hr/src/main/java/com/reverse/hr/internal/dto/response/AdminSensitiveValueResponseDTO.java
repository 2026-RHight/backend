package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.SensitiveFieldType;

public record AdminSensitiveValueResponseDTO(
        Long employeeId, SensitiveFieldType fieldType, String value) {}
