package com.reverse.payroll.internal.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeverancePayment {

    private Long id;
    private Long employeeId;
    private LocalDate retirementDate;
    private long serviceDays;
    private BigDecimal serviceYears;
    private BigDecimal averageMonthlyWage;
    private BigDecimal estimatedSeveranceAmount;
    private BigDecimal paidAmount;
    private LocalDate paymentDate;
    private String bankNameSnapshot;
    private String accountNumberSnapshotEnc;
    private String accountHolderSnapshot;
    private String note;
    private Long paidByEmployeeId;
    private LocalDateTime createdAt;

    @Builder
    public SeverancePayment(
            Long id,
            Long employeeId,
            LocalDate retirementDate,
            long serviceDays,
            BigDecimal serviceYears,
            BigDecimal averageMonthlyWage,
            BigDecimal estimatedSeveranceAmount,
            BigDecimal paidAmount,
            LocalDate paymentDate,
            String bankNameSnapshot,
            String accountNumberSnapshotEnc,
            String accountHolderSnapshot,
            String note,
            Long paidByEmployeeId,
            LocalDateTime createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.retirementDate = retirementDate;
        this.serviceDays = serviceDays;
        this.serviceYears = serviceYears;
        this.averageMonthlyWage = averageMonthlyWage;
        this.estimatedSeveranceAmount = estimatedSeveranceAmount;
        this.paidAmount = paidAmount;
        this.paymentDate = paymentDate;
        this.bankNameSnapshot = bankNameSnapshot;
        this.accountNumberSnapshotEnc = accountNumberSnapshotEnc;
        this.accountHolderSnapshot = accountHolderSnapshot;
        this.note = note;
        this.paidByEmployeeId = paidByEmployeeId;
        this.createdAt = createdAt;
    }
}
