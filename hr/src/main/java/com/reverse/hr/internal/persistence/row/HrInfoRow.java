package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.RecruitType;

import java.time.LocalDate;

public record HrInfoRow (
        String orgName,
        String positionName,
        String rankName,
        String jobName,
        EmployeeState employeeState,
        LocalDate hireDate,
        EmployType employType,
        RecruitType recruitType,
        String areaName
) {
}
