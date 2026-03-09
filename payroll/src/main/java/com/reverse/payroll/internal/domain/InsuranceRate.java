package com.reverse.payroll.internal.domain;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InsuranceRate {

    private Long insuranceId;
    private int applyYear;
    private BigDecimal nationalPensionRate;
    private BigDecimal healthInsuranceRate;
    private BigDecimal longTermCareRate;
    private BigDecimal empInsuranceRate;

    @Builder
    public InsuranceRate(
            int applyYear,
            BigDecimal nationalPensionRate,
            BigDecimal healthInsuranceRate,
            BigDecimal longTermCareRate,
            BigDecimal empInsuranceRate) {
        this.applyYear = applyYear;
        this.nationalPensionRate = nationalPensionRate;
        this.healthInsuranceRate = healthInsuranceRate;
        this.longTermCareRate = longTermCareRate;
        this.empInsuranceRate = empInsuranceRate;
    }
}
