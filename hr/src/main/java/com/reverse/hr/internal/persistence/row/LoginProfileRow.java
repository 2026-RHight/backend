package com.reverse.hr.internal.persistence.row;

public record LoginProfileRow(
        Long employeeId,
        String employeeNum,
        String employeeName,
        String orgName,
        String positionName,
        String rankName,
        String jobName
) {}
