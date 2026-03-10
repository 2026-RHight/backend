package com.reverse.payroll.internal.dto.response;

import com.reverse.payroll.internal.domain.PayrollLedger;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PayrollListResponse {

    private Long id;
    private String targetMonth;
    private BigDecimal totalPayment; // 세전
    private BigDecimal netPay; // 세후

    @Builder
    public PayrollListResponse(
            Long id, String targetMonth, BigDecimal totalPayment, BigDecimal netPay) {
        this.id = id;
        this.targetMonth = targetMonth;
        this.totalPayment = totalPayment;
        this.netPay = netPay;
    }

    public static PayrollListResponse from(PayrollLedger ledger) {
        return PayrollListResponse.builder()
                .id(ledger.getId())
                .targetMonth(ledger.getTargetMonth())
                .totalPayment(ledger.getTotalPayment())
                .netPay(ledger.getNetPay())
                .build();
    }
}
