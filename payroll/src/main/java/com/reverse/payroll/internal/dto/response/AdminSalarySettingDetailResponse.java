package com.reverse.payroll.internal.dto.response;

import com.reverse.payroll.internal.domain.SalarySetting;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminSalarySettingDetailResponse {

    private Long id;
    private Long employeeId;
    private BigDecimal baseSalary;
    private BigDecimal mealAllowance;
    private LocalDate applyStartDate;
    private LocalDate applyEndDate;
    private String bankName;
    private String maskedAccountNumber;
    private String accountHolder;

    @Builder
    public AdminSalarySettingDetailResponse(
            Long id,
            Long employeeId,
            BigDecimal baseSalary,
            BigDecimal mealAllowance,
            LocalDate applyStartDate,
            LocalDate applyEndDate,
            String bankName,
            String maskedAccountNumber,
            String accountHolder) {
        this.id = id;
        this.employeeId = employeeId;
        this.baseSalary = baseSalary;
        this.mealAllowance = mealAllowance;
        this.applyStartDate = applyStartDate;
        this.applyEndDate = applyEndDate;
        this.bankName = bankName;
        this.maskedAccountNumber = maskedAccountNumber;
        this.accountHolder = accountHolder;
    }

    public static AdminSalarySettingDetailResponse of(
            SalarySetting salarySetting, String maskedAccountNumber) {
        return AdminSalarySettingDetailResponse.builder()
                .id(salarySetting.getId())
                .employeeId(salarySetting.getEmployeeId())
                .baseSalary(salarySetting.getBaseSalary())
                .mealAllowance(salarySetting.getMealAllowance())
                .applyStartDate(salarySetting.getApplyStartDate())
                .applyEndDate(salarySetting.getApplyEndDate())
                .bankName(salarySetting.getBankName())
                .maskedAccountNumber(maskedAccountNumber)
                .accountHolder(salarySetting.getAccountHolder())
                .build();
    }
}
