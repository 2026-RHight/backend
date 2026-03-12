package com.reverse.payroll.internal.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminPayrollLedgerSummaryResponse {
    private String targetMonth;
    private int totalCount;
    private int finalizedCount;
    private int pendingFinalizeCount;
    private int sentCount;
    private int pendingSendCount;
}
