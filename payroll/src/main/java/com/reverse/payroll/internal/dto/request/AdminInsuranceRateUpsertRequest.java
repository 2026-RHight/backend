package com.reverse.payroll.internal.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminInsuranceRateUpsertRequest {

    @Min(value = 2000, message = "적용 연도는 2000년 이후여야 합니다.")
    @Max(value = 2100, message = "적용 연도는 2100년 이하여야 합니다.")
    private int applyYear;

    @NotNull(message = "국민연금율은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = true, message = "국민연금율은 0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", inclusive = true, message = "국민연금율은 1 이하여야 합니다.")
    private BigDecimal nationalPensionRate;

    @NotNull(message = "건강보험율은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = true, message = "건강보험율은 0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", inclusive = true, message = "건강보험율은 1 이하여야 합니다.")
    private BigDecimal healthInsuranceRate;

    @NotNull(message = "장기요양율은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = true, message = "장기요양율은 0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", inclusive = true, message = "장기요양율은 1 이하여야 합니다.")
    private BigDecimal longTermCareRate;

    @NotNull(message = "고용보험율은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = true, message = "고용보험율은 0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", inclusive = true, message = "고용보험율은 1 이하여야 합니다.")
    private BigDecimal empInsuranceRate;
}
