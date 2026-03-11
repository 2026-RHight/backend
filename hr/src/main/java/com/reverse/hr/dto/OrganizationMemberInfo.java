package com.reverse.hr.dto;

public record OrganizationMemberInfo(
        Long employeeId,
        String employeeName,
        Long orgId,
        String orgName,
        Long positionId,
        String positionName,
        String rankName,
        String jobName) {}
