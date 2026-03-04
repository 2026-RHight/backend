package com.reverse.hr.internal;

public record EmployeeProfileFacadeResponse(
        Long employeeId,
        String employeeName,
        String email,
        String orgName,
        String rankName,
        String positionName,
        String jobName
) {
}
