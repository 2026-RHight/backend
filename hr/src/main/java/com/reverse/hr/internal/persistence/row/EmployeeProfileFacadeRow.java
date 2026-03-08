package com.reverse.hr.internal.persistence.row;

public record EmployeeProfileFacadeRow(
        Long employeeId,
        String employeeName,
        String email,
        String orgName,
        String rankName,
        String positionName,
        String jobName) {}
