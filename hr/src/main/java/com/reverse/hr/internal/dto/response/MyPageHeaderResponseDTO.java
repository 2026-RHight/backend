package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.EmployeeState;

public record MyPageHeaderResponseDTO(
        String employeeName,
        EmployeeState employeeState,
        String orgName,
        String jobName,
        String positionName,
        String email,
        String phone,
        String extensionNum,
        String areaName,
        String profileFileUrl
) {
}
