package com.reverse.payroll.internal.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayrollLedger {
    private Long id;
    private Long employeeId;
    private Long insuranceId;
    private String yearMonth;
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

    @Builder
    public PayrollLedger(Long employeeId, Long insuranceId, String yearMonth,
                        BigDecimal salaryAmount, BigDecimal overtimeAmount
                        , BigDecimal mealAmount, BigDecimal totalPayment
                        , BigDecimal netPay, String isFinalized, String isSent
                        , BigDecimal nationalPensionAmount, BigDecimal healthInsuranceAmount
                        , BigDecimal longTermCareAmount, BigDecimal empInsuranceAmount
                        , BigDecimal incomeTaxAmount, BigDecimal localTaxAmount)
    {
        this.employeeId = employeeId;
        this.insuranceId = insuranceId;
        this.yearMonth = yearMonth;
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
    }

}
