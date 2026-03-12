package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import java.time.LocalDate;

public record HrChangePendingEventRow(
        Long hrEventId,
        Long employeeId,
        Long targetOrgId,
        Long targetJobId,
        Long targetPositionId,
        Long targetRankId,
        EmployeeState targetEmployeeState,
        EmployType targetEmployType,
        Long targetAreaId,
        LocalDate targetEffectiveFrom,
        String targetRoleIdsJson) {}
