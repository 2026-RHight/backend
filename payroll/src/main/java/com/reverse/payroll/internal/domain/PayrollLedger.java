package com.reverse.payroll.internal.domain;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayrollLedger {
    private Long id;
    private Long employeeId;
    private Long insuranceId;
    private String targetMonth;
    private BigDecimal salaryAmount;
    private BigDecimal overtimeAmount;
    private BigDecimal mealAmount;
    private BigDecimal totalPayment;
    private BigDecimal netPay;
    private String isFinalized;
    private String isSent; // 명세서발송
    private BigDecimal nationalPensionAmount; // 국민연금금액
    private BigDecimal healthInsuranceAmount; // 건강보험금액
    private BigDecimal longTermCareAmount; // 장기요양금액
    private BigDecimal empInsuranceAmount; // 고용보험금액
    private BigDecimal incomeTaxAmount; // 소득세금액
    private BigDecimal localTaxAmount; // 지방소득세금액

    // 스냅샷 정보 (급여 확정 시점의 인사 정보)
    private String employeeNameSnapshot;
    private String deptNameSnapshot;
    private String positionNameSnapshot;
    private String bankNameSnapshot;
    private String accountNumberSnapshotEnc;
    private String accountHolderSnapshot;

    @Builder
    public PayrollLedger(
            Long id,
            Long employeeId,
            Long insuranceId,
            String targetMonth,
            BigDecimal salaryAmount,
            BigDecimal overtimeAmount,
            BigDecimal mealAmount,
            BigDecimal totalPayment,
            BigDecimal netPay,
            String isFinalized,
            String isSent,
            BigDecimal nationalPensionAmount,
            BigDecimal healthInsuranceAmount,
            BigDecimal longTermCareAmount,
            BigDecimal empInsuranceAmount,
            BigDecimal incomeTaxAmount,
            BigDecimal localTaxAmount,
            String employeeNameSnapshot,
            String deptNameSnapshot,
            String positionNameSnapshot,
            String bankNameSnapshot,
            String accountNumberSnapshotEnc,
            String accountHolderSnapshot) {
        this.id = id;
        this.employeeId = employeeId;
        this.insuranceId = insuranceId;
        this.targetMonth = targetMonth;
        this.salaryAmount = salaryAmount;
        this.overtimeAmount = overtimeAmount;
        this.mealAmount = mealAmount;
        this.totalPayment = totalPayment;
        this.netPay = netPay;
        this.isFinalized = isFinalized;
        this.isSent = isSent;
        this.nationalPensionAmount = nationalPensionAmount;
        this.healthInsuranceAmount = healthInsuranceAmount;
        this.longTermCareAmount = longTermCareAmount;
        this.empInsuranceAmount = empInsuranceAmount;
        this.incomeTaxAmount = incomeTaxAmount;
        this.localTaxAmount = localTaxAmount;
        this.employeeNameSnapshot = employeeNameSnapshot;
        this.deptNameSnapshot = deptNameSnapshot;
        this.positionNameSnapshot = positionNameSnapshot;
        this.bankNameSnapshot = bankNameSnapshot;
        this.accountNumberSnapshotEnc = accountNumberSnapshotEnc;
        this.accountHolderSnapshot = accountHolderSnapshot;
    }
}
