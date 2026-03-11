package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.RecruitType;
import java.time.LocalDate;

public record AdminEmployeeDetailRow(
        Long employeeId,
        String employeeNum,
        String employeeName,
        String profileFileUrl,
        String email,
        String phone,
        String extensionNum,
        String address,
        LocalDate birthDate,
        String orgName,
        String positionName,
        String jobName,
        String rankName,
        EmployeeState employeeState,
        LocalDate hireDate,
        EmployType employType,
        RecruitType recruitType,
        String areaName,
        String bankName,
        String residentNumberEnc,
        String accountNumberEnc) {}
