package com.reverse.payroll.internal.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminSalarySettingUpsertRequest {

    @NotNull(message = "기본급은 필수입니다.")
    @DecimalMin(value = "0.00", message = "기본급은 0 이상이어야 합니다.")
    private BigDecimal baseSalary;

    @NotNull(message = "식대는 필수입니다.")
    @DecimalMin(value = "0.00", message = "식대는 0 이상이어야 합니다.")
    private BigDecimal mealAllowance;

    @NotNull(message = "적용 시작일은 필수입니다.")
    private LocalDate applyStartDate;

    private LocalDate applyEndDate;
}
