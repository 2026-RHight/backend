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

    // 이 정보들은 사원 테이블에서 조회하여 매핑합니다.
    private String bankName;
    private String accountNumberEnc; // 암호화된 계좌번호
    private String accountHolder; // 통상 사원명

    private LocalDate applyStartDate; // 적용시작일
    private LocalDate applyEndDate; // 적용종료일

    @Builder
    public SalarySetting(
            Long id,
            Long employeeId,
            BigDecimal baseSalary,
            BigDecimal mealAllowance,
            String bankName,
            String accountNumberEnc,
            String accountHolder,
            LocalDate applyStartDate,
            LocalDate applyEndDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.baseSalary = baseSalary;
        this.mealAllowance = mealAllowance;
        this.bankName = bankName;
        this.accountNumberEnc = accountNumberEnc;
        this.accountHolder = accountHolder;
        this.applyStartDate = applyStartDate;
        this.applyEndDate = applyEndDate;
    }
}
