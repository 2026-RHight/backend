package com.reverse.payroll.internal.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminSeverancePreviewResponse {

    private Long employeeId;
    private String employeeNum;
    private String employeeName;
    private String departmentName;
    private String positionName;
    private String employState;
    private LocalDate hireDate;
    private LocalDate retirementDate;
    private long serviceDays;
    private BigDecimal serviceYears;
    private boolean eligible;
    private int referenceMonthCount;
    private String referenceStartMonth;
    private String referenceEndMonth;
    private BigDecimal averageMonthlyWage;
    private BigDecimal estimatedSeveranceAmount;
    private String bankName;
    private String maskedAccountNumber;
    private String accountHolder;
    private String note;

    @Builder
    public AdminSeverancePreviewResponse(
            Long employeeId,
            String employeeNum,
            String employeeName,
            String departmentName,
            String positionName,
            String employState,
            LocalDate hireDate,
            LocalDate retirementDate,
            long serviceDays,
            BigDecimal serviceYears,
            boolean eligible,
            int referenceMonthCount,
            String referenceStartMonth,
            String referenceEndMonth,
            BigDecimal averageMonthlyWage,
            BigDecimal estimatedSeveranceAmount,
            String bankName,
            String maskedAccountNumber,
            String accountHolder,
            String note) {
        this.employeeId = employeeId;
        this.employeeNum = employeeNum;
        this.employeeName = employeeName;
        this.departmentName = departmentName;
        this.positionName = positionName;
        this.employState = employState;
        this.hireDate = hireDate;
        this.retirementDate = retirementDate;
        this.serviceDays = serviceDays;
        this.serviceYears = serviceYears;
        this.eligible = eligible;
        this.referenceMonthCount = referenceMonthCount;
        this.referenceStartMonth = referenceStartMonth;
        this.referenceEndMonth = referenceEndMonth;
        this.averageMonthlyWage = averageMonthlyWage;
        this.estimatedSeveranceAmount = estimatedSeveranceAmount;
        this.bankName = bankName;
        this.maskedAccountNumber = maskedAccountNumber;
        this.accountHolder = accountHolder;
        this.note = note;
    }
}
