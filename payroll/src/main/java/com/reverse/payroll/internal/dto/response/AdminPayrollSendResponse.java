package com.reverse.payroll.internal.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminPayrollSendResponse {

    private Long ledgerId;
    private String targetMonth;
    private int sentCount;
    private int alreadySentCount;
    private String message;

    @Builder
    public AdminPayrollSendResponse(
            Long ledgerId,
            String targetMonth,
            int sentCount,
            int alreadySentCount,
            String message) {
        this.ledgerId = ledgerId;
        this.targetMonth = targetMonth;
        this.sentCount = sentCount;
        this.alreadySentCount = alreadySentCount;
        this.message = message;
    }
}
