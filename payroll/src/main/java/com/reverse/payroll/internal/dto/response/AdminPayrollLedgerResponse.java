package com.reverse.payroll.internal.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminPayrollLedgerResponse {

    private Long id;
    private Long employeeId;
    private String targetMonth;
    private String employeeName;
    private String departmentName;
    private String positionName;
    private String bankName;
    private String maskedAccountNumber;
    private String accountNumber;
    private String accountHolder;
    private BigDecimal salaryAmount;
    private BigDecimal overtimeAmount;
    private BigDecimal mealAmount;
    private BigDecimal totalPayment;
    private BigDecimal totalDeductionAmount;
    private BigDecimal netPay;
    private String isFinalized;
    private String isSent;
    private String sendStatusDescription;

    @Builder
    public AdminPayrollLedgerResponse(
            Long id,
            Long employeeId,
            String targetMonth,
            String employeeName,
            String departmentName,
            String positionName,
            String bankName,
            String maskedAccountNumber,
            String accountNumber,
            String accountHolder,
            BigDecimal salaryAmount,
            BigDecimal overtimeAmount,
            BigDecimal mealAmount,
            BigDecimal totalPayment,
            BigDecimal totalDeductionAmount,
            BigDecimal netPay,
            String isFinalized,
            String isSent,
            String sendStatusDescription) {
        this.id = id;
        this.employeeId = employeeId;
        this.targetMonth = targetMonth;
        this.employeeName = employeeName;
        this.departmentName = departmentName;
        this.positionName = positionName;
        this.bankName = bankName;
        this.maskedAccountNumber = maskedAccountNumber;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.salaryAmount = salaryAmount;
        this.overtimeAmount = overtimeAmount;
        this.mealAmount = mealAmount;
        this.totalPayment = totalPayment;
        this.totalDeductionAmount = totalDeductionAmount;
        this.netPay = netPay;
        this.isFinalized = isFinalized;
        this.isSent = isSent;
        this.sendStatusDescription = sendStatusDescription;
    }
}
