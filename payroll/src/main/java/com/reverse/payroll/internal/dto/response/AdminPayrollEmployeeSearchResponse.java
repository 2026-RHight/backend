package com.reverse.payroll.internal.dto.response;

import com.reverse.payroll.internal.persistence.PayrollMapper;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminPayrollEmployeeSearchResponse {

    private Long employeeId;
    private String employeeNum;
    private String employeeName;
    private String departmentName;
    private String positionName;
    private String employState;

    @Builder
    public AdminPayrollEmployeeSearchResponse(
            Long employeeId,
            String employeeNum,
            String employeeName,
            String departmentName,
            String positionName,
            String employState) {
        this.employeeId = employeeId;
        this.employeeNum = employeeNum;
        this.employeeName = employeeName;
        this.departmentName = departmentName;
        this.positionName = positionName;
        this.employState = employState;
    }

    public static AdminPayrollEmployeeSearchResponse from(PayrollMapper.EmployeeSearchRow row) {
        return AdminPayrollEmployeeSearchResponse.builder()
                .employeeId(row.employeeId())
                .employeeNum(row.employeeNum())
                .employeeName(row.employeeName())
                .departmentName(row.departmentName())
                .positionName(row.positionName())
                .employState(row.employState())
                .build();
    }
}
