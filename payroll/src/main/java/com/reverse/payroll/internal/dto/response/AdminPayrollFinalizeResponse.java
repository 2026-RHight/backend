package com.reverse.payroll.internal.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminPayrollFinalizeResponse {

    private String targetMonth;
    private int totalCount;
    private int finalizedCount;
    private int alreadyFinalizedCount;

    @Builder
    public AdminPayrollFinalizeResponse(
            String targetMonth, int totalCount, int finalizedCount, int alreadyFinalizedCount) {
        this.targetMonth = targetMonth;
        this.totalCount = totalCount;
        this.finalizedCount = finalizedCount;
        this.alreadyFinalizedCount = alreadyFinalizedCount;
    }
}
