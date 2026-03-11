package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import java.time.LocalDate;
import java.util.List;

public record HrChangeCurrentInfoResponseDTO(
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
        String employeeStateDescription,
        EmployType employType,
        String employTypeDescription,
        Long areaId,
        String areaName,
        LocalDate effectiveFrom,
        List<Long> roleIds,
        List<String> roleCodes) {}
