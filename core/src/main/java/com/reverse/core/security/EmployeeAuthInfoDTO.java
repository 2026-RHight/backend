package com.reverse.core.security;

public record EmployeeAuthInfoDTO(
        Long employeeId,
        String employeeNum,
        String password
) {}