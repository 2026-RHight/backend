package com.reverse.hr.dto;

public record EmployeeProfileDTO(
        Long employeeId,
        String employeeName,
        String email,
        String orgName,
        String rankName,
        String positionName,
        String jobName
) {
}
