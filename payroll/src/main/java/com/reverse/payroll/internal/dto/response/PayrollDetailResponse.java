package com.reverse.payroll.internal.dto.response;

import com.reverse.payroll.internal.domain.PayrollLedger;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PayrollDetailResponse {

    private Long id;
    private String targetMonth;

    // 사원 정보 (급여명세서용 추가)
    private String employeeName;
    private String department;
    private String position;
    private String paymentDate;

    // 지급 내역
    private BigDecimal salaryAmount; // 기본급
    private BigDecimal overtimeAmount; // 연장수당
    private BigDecimal mealAmount;
    private BigDecimal totalPayment; // 세전

    // 공제 내역
    private BigDecimal nationalPensionAmount; // 국민연금금액
    private BigDecimal healthInsuranceAmount; // 건강보험금액
    private BigDecimal longTermCareAmount; // 장기요양금액
    private BigDecimal empInsuranceAmount; // 고용보험금액
    private BigDecimal incomeTaxAmount; // 소득세금액
    private BigDecimal localTaxAmount; // 지방소득세금액
    private BigDecimal totalDeductionAmount; // 총 공제액 합계

    // 입금 계좌 정보 (ERD 추가분)
    private String bankName;
    private String accountNumber;
    private String accountHolder;

    // 세후
    private BigDecimal netPay;

    @Builder
    public PayrollDetailResponse(
            Long id,
            String targetMonth,
            String employeeName,
            String department,
            String position,
            String paymentDate,
            BigDecimal salaryAmount,
            BigDecimal overtimeAmount,
            BigDecimal mealAmount,
            BigDecimal totalPayment,
            BigDecimal nationalPensionAmount,
            BigDecimal healthInsuranceAmount,
            BigDecimal longTermCareAmount,
            BigDecimal empInsuranceAmount,
            BigDecimal incomeTaxAmount,
            BigDecimal localTaxAmount,
            BigDecimal totalDeductionAmount,
            BigDecimal netPay,
            String bankName,
            String accountNumber,
            String accountHolder) {
        this.id = id;
        this.targetMonth = targetMonth;
        this.employeeName = employeeName;
        this.department = department;
        this.position = position;
        this.paymentDate = paymentDate;
        this.salaryAmount = salaryAmount;
        this.overtimeAmount = overtimeAmount;
        this.mealAmount = mealAmount;
        this.totalPayment = totalPayment;
        this.nationalPensionAmount = nationalPensionAmount;
        this.healthInsuranceAmount = healthInsuranceAmount;
        this.longTermCareAmount = longTermCareAmount;
        this.empInsuranceAmount = empInsuranceAmount;
        this.incomeTaxAmount = incomeTaxAmount;
        this.localTaxAmount = localTaxAmount;
        this.totalDeductionAmount = totalDeductionAmount;
        this.netPay = netPay;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
    }

    public static PayrollDetailResponse from(PayrollLedger ledger) {
        return of(ledger, null, null, null, null, null);
    }

    public static PayrollDetailResponse of(
            PayrollLedger ledger,
            com.reverse.payroll.internal.domain.SalarySetting salarySetting,
            String plainAccountNumber,
            String employeeName,
            String department,
            String position) {
        BigDecimal totalDeduction =
                safeAdd(
                        ledger.getNationalPensionAmount(),
                        ledger.getHealthInsuranceAmount(),
                        ledger.getLongTermCareAmount(),
                        ledger.getEmpInsuranceAmount(),
                        ledger.getIncomeTaxAmount(),
                        ledger.getLocalTaxAmount());

        // 지급일은 통상 해당월 25일로 표기 (가정)
        String paymentDate = ledger.getTargetMonth() + "-25";

        return PayrollDetailResponse.builder()
                .id(ledger.getId())
                .targetMonth(ledger.getTargetMonth())
                .employeeName(employeeName)
                .department(department)
                .position(position)
                .paymentDate(paymentDate)
                .salaryAmount(ledger.getSalaryAmount())
                .overtimeAmount(ledger.getOvertimeAmount())
                .mealAmount(ledger.getMealAmount())
                .totalPayment(ledger.getTotalPayment())
                .nationalPensionAmount(ledger.getNationalPensionAmount())
                .healthInsuranceAmount(ledger.getHealthInsuranceAmount())
                .longTermCareAmount(ledger.getLongTermCareAmount())
                .empInsuranceAmount(ledger.getEmpInsuranceAmount())
                .incomeTaxAmount(ledger.getIncomeTaxAmount())
                .localTaxAmount(ledger.getLocalTaxAmount())
                .totalDeductionAmount(totalDeduction)
                .netPay(ledger.getNetPay())
                .bankName(
                        ledger.getBankNameSnapshot() != null
                                ? ledger.getBankNameSnapshot()
                                : (salarySetting != null ? salarySetting.getBankName() : null))
                .accountNumber(plainAccountNumber)
                .accountHolder(
                        ledger.getAccountHolderSnapshot() != null
                                ? ledger.getAccountHolderSnapshot()
                                : (salarySetting != null ? salarySetting.getAccountHolder() : null))
                .build();
    }

    private static BigDecimal safeAdd(BigDecimal... values) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            if (value != null) {
                sum = sum.add(value);
            }
        }
        return sum;
    }
}
