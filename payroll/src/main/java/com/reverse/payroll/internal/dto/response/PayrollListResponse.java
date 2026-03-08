package com.reverse.payroll.internal.dto.response;

import com.reverse.payroll.internal.domain.PayrollLedger;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PayrollListResponse {

    private Long id;
    private String yearMonth;
    private BigDecimal totalPayment; // 세전
    private BigDecimal netPay; // 세후

    @Builder
    public PayrollListResponse(Long id, String yearMonth, BigDecimal totalPayment, BigDecimal netPay) {
        this.id = id;
        this.yearMonth = yearMonth;
        this.totalPayment = totalPayment;
        this.netPay = netPay;
    }

    public static PayrollListResponse from(PayrollLedger ledger) {
        return PayrollListResponse.builder()
                .id(ledger.getId())
                .yearMonth(ledger.getYearMonth())
                .totalPayment(ledger.getTotalPayment())
                .netPay(ledger.getNetPay())
                .build();
    }

}
