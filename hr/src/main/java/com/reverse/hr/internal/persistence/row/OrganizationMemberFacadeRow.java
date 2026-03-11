package com.reverse.hr.internal.persistence.row;

public record OrganizationMemberFacadeRow(
        Long employeeId,
        String employeeName,
        Long orgId,
        String orgName,
        Long positionId,
        String positionName,
        String rankName,
        String jobName) {}
