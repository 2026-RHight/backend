package com.reverse.payroll.internal.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminPayrollBatchCalculateFailureResponse {

    private Long employeeId;
    private String reason;

    @Builder
    public AdminPayrollBatchCalculateFailureResponse(Long employeeId, String reason) {
        this.employeeId = employeeId;
        this.reason = reason;
    }
}
