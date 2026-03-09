package com.reverse.payroll.internal.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InsuranceRate {

    private Long insuranceId;
    private int applyYear;
    private double nationalPensionRate;
    private double healthInsuranceRate;
    private double longTermCareRate;
    private double empInsuranceRate;

    @Builder
    public InsuranceRate(
            int applyYear,
            double nationalPensionRate,
            double healthInsuranceRate,
            double longTermCareRate,
            double empInsuranceRate) {
        this.applyYear = applyYear;
        this.nationalPensionRate = nationalPensionRate;
        this.healthInsuranceRate = healthInsuranceRate;
        this.longTermCareRate = longTermCareRate;
        this.empInsuranceRate = empInsuranceRate;
    }
}
