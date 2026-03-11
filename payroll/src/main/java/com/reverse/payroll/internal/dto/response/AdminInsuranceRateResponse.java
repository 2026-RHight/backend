package com.reverse.payroll.internal.dto.response;

import com.reverse.payroll.internal.domain.InsuranceRate;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminInsuranceRateResponse {

    private Long insuranceId;
    private int applyYear;
    private BigDecimal nationalPensionRate;
    private BigDecimal healthInsuranceRate;
    private BigDecimal longTermCareRate;
    private BigDecimal empInsuranceRate;

    @Builder
    public AdminInsuranceRateResponse(
            Long insuranceId,
            int applyYear,
            BigDecimal nationalPensionRate,
            BigDecimal healthInsuranceRate,
            BigDecimal longTermCareRate,
            BigDecimal empInsuranceRate) {
        this.insuranceId = insuranceId;
        this.applyYear = applyYear;
        this.nationalPensionRate = nationalPensionRate;
        this.healthInsuranceRate = healthInsuranceRate;
        this.longTermCareRate = longTermCareRate;
        this.empInsuranceRate = empInsuranceRate;
    }

    public static AdminInsuranceRateResponse from(InsuranceRate insuranceRate) {
        return AdminInsuranceRateResponse.builder()
                .insuranceId(insuranceRate.getInsuranceId())
                .applyYear(insuranceRate.getApplyYear())
                .nationalPensionRate(insuranceRate.getNationalPensionRate())
                .healthInsuranceRate(insuranceRate.getHealthInsuranceRate())
                .longTermCareRate(insuranceRate.getLongTermCareRate())
                .empInsuranceRate(insuranceRate.getEmpInsuranceRate())
                .build();
    }
}
