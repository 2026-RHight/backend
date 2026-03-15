package com.reverse.hr.internal.dto.response;

public record TeamBirthdayResponseDTO(
        Long employeeId, String employeeName, String birthday, Integer daysRemaining) {}
