package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import java.time.LocalDate;

public record AdminEmployeeListRow(
        Long employeeId,
        String employeeNum,
        String employeeName,
        String profileFileUrl,
        String orgName,
        String positionName,
        String jobName,
        String rankName,
        EmployeeState employeeState,
        LocalDate hireDate,
        EmployType employType,
        String areaName) {}
