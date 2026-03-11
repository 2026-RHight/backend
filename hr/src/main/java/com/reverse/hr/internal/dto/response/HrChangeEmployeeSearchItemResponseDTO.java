package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;

public record HrChangeEmployeeSearchItemResponseDTO(
        Long employeeId,
        String employeeNum,
        String employeeName,
        String orgName,
        String positionName,
        String jobName,
        String rankName,
        EmployeeState employeeState,
        String employeeStateDescription,
        EmployType employType,
        String employTypeDescription,
        String areaName) {}
