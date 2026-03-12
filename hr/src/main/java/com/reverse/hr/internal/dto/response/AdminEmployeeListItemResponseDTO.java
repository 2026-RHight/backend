package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import java.time.LocalDate;

public record AdminEmployeeListItemResponseDTO(
        Long employeeId,
        String employeeNum,
        String employeeName,
        String profileFileUrl,
        String orgName,
        String positionName,
        String jobName,
        String rankName,
        EmployeeState employeeState,
        String employeeStateDescription,
        LocalDate hireDate,
        EmployType employType,
        String employTypeDescription,
        String areaName) {}
