package com.reverse.hr.internal.dto.response;

public record AdminEmployeeCreateResponseDTO(
        Long employeeId, String employeeNum, String employeeName, boolean initialState) {}
