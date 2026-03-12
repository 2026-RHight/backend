package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;

public record HrChangeEmployeeSearchRow(
        Long employeeId,
        String employeeNum,
        String employeeName,
        String orgName,
        String positionName,
        String jobName,
        String rankName,
        EmployeeState employeeState,
        EmployType employType,
        String areaName) {}
