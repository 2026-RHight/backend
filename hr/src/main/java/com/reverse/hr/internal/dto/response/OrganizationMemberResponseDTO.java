package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.EmployeeState;

public record OrganizationMemberResponseDTO(
        Long employeeId,
        String employeeName,
        String email,
        String phone,
        String extensionNum,
        String positionName,
        String jobName,
        String rankName,
        String areaName,
        EmployeeState employeeState) {}
