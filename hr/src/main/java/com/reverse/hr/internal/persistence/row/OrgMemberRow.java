package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.EmployeeState;

public record OrgMemberRow(
        Long employeeId,
        String employeeName,
        Long orgId,
        String orgName,
        String profileFileUrl,
        String email,
        String phone,
        String extensionNum,
        String positionName,
        String jobName,
        String rankName,
        String areaName,
        EmployeeState employeeState,
        Integer leaderSort,
        Long rankNo) {}
