package com.reverse.payroll.internal.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminSeverancePaymentResponse {

    private Long severancePaymentId;
    private Long employeeId;
    private String employeeName;
    private LocalDate retirementDate;
    private LocalDate paymentDate;
    private BigDecimal paidAmount;
    private String message;

    @Builder
    public AdminSeverancePaymentResponse(
            Long severancePaymentId,
            Long employeeId,
            String employeeName,
            LocalDate retirementDate,
            LocalDate paymentDate,
            BigDecimal paidAmount,
            String message) {
        this.severancePaymentId = severancePaymentId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.retirementDate = retirementDate;
        this.paymentDate = paymentDate;
        this.paidAmount = paidAmount;
        this.message = message;
    }
}
