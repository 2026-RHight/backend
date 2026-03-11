package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.RecruitType;
import java.time.LocalDate;

public record AdminEmployeeDetailResponseDTO(
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
        String employeeStateDescription,
        LocalDate hireDate,
        EmployType employType,
        String employTypeDescription,
        RecruitType recruitType,
        String recruitTypeDescription,
        String areaName,
        String bankName,
        String residentNumberMasked,
        String accountNumberMasked) {}
