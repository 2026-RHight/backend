package com.reverse.payroll.internal.dto.response;

import com.reverse.payroll.internal.domain.PayrollLedger;
import com.reverse.payroll.internal.domain.SalarySetting;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PayrollDetailResponse {

    private Long id;
    private String yearMonth;
    private String employeeName;
    private String department;
    private String position;
    private String paymentDate;
    private String bankName;
    private String accountNumber;
    private String accountHolder;

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

    // 세후
    private BigDecimal netPay;

    @Builder
    public PayrollDetailResponse(
            Long id,
            String yearMonth,
            String employeeName,
            String department,
            String position,
            String paymentDate,
            String bankName,
            String accountNumber,
            String accountHolder,
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
            BigDecimal netPay) {
        this.id = id;
        this.yearMonth = yearMonth;
        this.employeeName = employeeName;
        this.department = department;
        this.position = position;
        this.paymentDate = paymentDate;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
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
    }

    public static PayrollDetailResponse from(PayrollLedger ledger) {
        return of(ledger, null, null, null, null, null);
    }

    public static PayrollDetailResponse of(
            PayrollLedger ledger,
            SalarySetting salarySetting,
            String plainAccountNumber,
            String empName,
            String deptName,
            String posName) {
        BigDecimal totalDeduction =
                safeAdd(
                        ledger.getNationalPensionAmount(),
                        ledger.getHealthInsuranceAmount(),
                        ledger.getLongTermCareAmount(),
                        ledger.getEmpInsuranceAmount(),
                        ledger.getIncomeTaxAmount(),
                        ledger.getLocalTaxAmount());

        String bankName =
                ledger.getBankNameSnapshot() != null
                        ? ledger.getBankNameSnapshot()
                        : (salarySetting != null ? salarySetting.getBankName() : null);
        String accountHolder =
                ledger.getAccountHolderSnapshot() != null
                        ? ledger.getAccountHolderSnapshot()
                        : (salarySetting != null ? salarySetting.getAccountHolder() : null);
        String paymentDate =
                LocalDate.parse(ledger.getTargetMonth() + "-01").withDayOfMonth(25).toString();

        return PayrollDetailResponse.builder()
                .id(ledger.getId())
                .yearMonth(ledger.getTargetMonth())
                .employeeName(empName)
                .department(deptName)
                .position(posName)
                .paymentDate(paymentDate)
                .bankName(bankName)
                .accountNumber(plainAccountNumber)
                .accountHolder(accountHolder)
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
