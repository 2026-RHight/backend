package com.reverse.payroll.internal.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminPayrollBatchCalculateResponse {

    private int targetCount;
    private int calculatedCount;
    private int skippedCount;
    private int failedCount;
    private List<AdminPayrollBatchCalculateFailureResponse> failures;

    @Builder
    public AdminPayrollBatchCalculateResponse(
            int targetCount,
            int calculatedCount,
            int skippedCount,
            int failedCount,
            List<AdminPayrollBatchCalculateFailureResponse> failures) {
        this.targetCount = targetCount;
        this.calculatedCount = calculatedCount;
        this.skippedCount = skippedCount;
        this.failedCount = failedCount;
        this.failures = failures;
    }
}
