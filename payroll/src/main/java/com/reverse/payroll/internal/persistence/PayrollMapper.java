package com.reverse.payroll.internal.persistence;

import com.reverse.payroll.internal.domain.InsuranceRate;
import com.reverse.payroll.internal.domain.PayrollLedger;
import com.reverse.payroll.internal.domain.SalarySetting;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PayrollMapper {

    // 사원 식별번호로 최근 유효한 기본 급여 설정 조회
    Optional<SalarySetting> findSalarySettingByEmployeeId(
            @Param("employeeId") Long employeeId, @Param("targetDate") LocalDate targetDate);

    // 적용년도 4대보험 요율 조회
    Optional<InsuranceRate> findInsuranceRateByApplyYear(@Param("applyYear") int applyYear);

    // 사원의 최근급여 명세서 목록 반환
    List<PayrollLedger> findRecentPayrollLedgersByEmployeeId(
            @Param("employeeId") Long employeeId, @Param("limit") int limit);

    // 특정 년도에 해당하는 사원의 급여 명세서 목록 반환
    List<PayrollLedger> findPayrollLedgersByYear(
            @Param("employeeId") Long employeeId, @Param("year") String year);

    // 급여 명세서 상세 조회
    Optional<PayrollLedger> findPayrollLedgerById(@Param("id") Long id);

    // 특정 사원급여 명세서 조회
    Optional<PayrollLedger> findPayrollLedgerByYearMonth(
            @Param("employeeId") Long employeeId, @Param("yearMonth") String yearMonth);

    // 급여대장 저장
    void insertPayrollLedger(PayrollLedger payrollLedger);

    // 사원 비밀번호(해시) 조회
    Optional<String> findEmployeePasswordById(@Param("employeeId") Long employeeId);
}
