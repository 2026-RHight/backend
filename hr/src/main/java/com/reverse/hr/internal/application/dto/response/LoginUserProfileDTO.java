package com.reverse.hr.internal.application.dto.response;

public record LoginUserProfileDTO(
        Long employeeId,
        String employeeNum,
        String employeeName,
        String orgName,
        String positionName,
        String rankName,
        String jobName
) {}
