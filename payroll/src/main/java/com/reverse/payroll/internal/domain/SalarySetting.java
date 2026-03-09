package com.reverse.payroll.internal.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalarySetting {

    private Long id;
    private Long employeeId;
    private BigDecimal baseSalary;
    private BigDecimal mealAllowance;
    private LocalDate applyStartDate; // 적용시작일
    private LocalDate applyEndDate; // 적용종료일

    @Builder
    public SalarySetting(
            Long employeeId,
            BigDecimal baseSalary,
            BigDecimal mealAllowance,
            LocalDate applyStartDate,
            LocalDate applyEndDate) {
        this.employeeId = employeeId;
        this.baseSalary = baseSalary;
        this.mealAllowance = mealAllowance;
        this.applyStartDate = applyStartDate;
        this.applyEndDate = applyEndDate;
    }
}
