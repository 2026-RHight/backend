package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import java.time.LocalDate;

public record HrChangeCurrentInfoRow(
        Long employeeId,
        String employeeNum,
        String employeeName,
        Long orgId,
        String orgName,
        Long jobId,
        String jobName,
        Long positionId,
        String positionName,
        Long rankId,
        String rankName,
        EmployeeState employeeState,
        EmployType employType,
        Long areaId,
        String areaName,
        LocalDate effectiveFrom) {}
