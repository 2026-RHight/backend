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

    List<Long> findEmployeeIdsWithSalarySetting(@Param("targetDate") LocalDate targetDate);

    List<SalarySetting> findSalarySettingHistoryByEmployeeId(@Param("employeeId") Long employeeId);

    Optional<SalarySetting> findSalarySettingById(@Param("id") Long id);

    List<EmployeeSearchRow> searchEmployees(
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countEmployees(@Param("keyword") String keyword);

    boolean existsSalarySettingOverlap(
            @Param("employeeId") Long employeeId,
            @Param("applyStartDate") LocalDate applyStartDate,
            @Param("applyEndDate") LocalDate applyEndDate,
            @Param("excludeId") Long excludeId);

    void insertSalarySetting(SalarySetting salarySetting);

    int updateSalarySetting(SalarySetting salarySetting);

    // 적용년도 4대보험 요율 조회
    Optional<InsuranceRate> findInsuranceRateByApplyYear(@Param("applyYear") int applyYear);

    List<InsuranceRate> findAllInsuranceRates();

    Optional<InsuranceRate> findInsuranceRateById(@Param("insuranceId") Long insuranceId);

    void insertInsuranceRate(InsuranceRate insuranceRate);

    int updateInsuranceRate(InsuranceRate insuranceRate);

    // 사원의 최근급여 명세서 목록 반환
    List<PayrollLedger> findRecentPayrollLedgersByEmployeeId(
            @Param("employeeId") Long employeeId, @Param("limit") int limit);

    // 특정 년도에 해당하는 사원의 급여 명세서 목록 반환
    List<PayrollLedger> findPayrollLedgersByYear(
            @Param("employeeId") Long employeeId, @Param("year") String year);

    // 급여 명세서 상세 조회
    Optional<PayrollLedger> findPayrollLedgerById(@Param("id") Long id);

    List<PayrollLedger> findPayrollLedgersByEmployeeIdAndMonthRange(
            @Param("employeeId") Long employeeId,
            @Param("startMonth") String startMonth,
            @Param("endMonth") String endMonth);

    // 특정 사원급여 명세서 조회
    Optional<PayrollLedger> findPayrollLedgerByYearMonth(
            @Param("employeeId") Long employeeId, @Param("targetMonth") String targetMonth);

    List<PayrollLedger> findAdminPayrollLedgersByMonth(
            @Param("targetMonth") String targetMonth,
            @Param("employeeName") String employeeName,
            @Param("departmentName") String departmentName,
            @Param("isFinalized") String isFinalized,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countAdminPayrollLedgersByMonth(
            @Param("targetMonth") String targetMonth,
            @Param("employeeName") String employeeName,
            @Param("departmentName") String departmentName,
            @Param("isFinalized") String isFinalized);

    int countPayrollLedgersByTargetMonth(@Param("targetMonth") String targetMonth);

    int countFinalizedPayrollLedgersByTargetMonth(@Param("targetMonth") String targetMonth);

    int finalizePayrollLedgersByTargetMonth(@Param("targetMonth") String targetMonth);

    int updatePayrollLedgerSent(@Param("ledgerId") Long ledgerId);

    int countSentPayrollLedgersByTargetMonth(@Param("targetMonth") String targetMonth);

    int updatePayrollLedgersSentByTargetMonth(@Param("targetMonth") String targetMonth);

    // 급여대장 저장
    void insertPayrollLedger(PayrollLedger payrollLedger);

    // 사원 비밀번호(해시) 조회
    Optional<String> findEmployeePasswordById(@Param("employeeId") Long employeeId);

    Optional<String> findEmployeeEmailById(@Param("employeeId") Long employeeId);

    // 급여명세서용 사원 기본 정보 조회
    Optional<EmployeePayslipInfo> findEmployeePayslipInfo(@Param("employeeId") Long employeeId);

    Optional<SeveranceEmployeeInfo> findEmployeeSeveranceInfo(@Param("employeeId") Long employeeId);

    record EmployeePayslipInfo(String employeeName, String departmentName, String positionName) {}

    record EmployeeSearchRow(
            Long employeeId,
            String employeeNum,
            String employeeName,
            String departmentName,
            String positionName,
            String employState) {}

    record SeveranceEmployeeInfo(
            Long employeeId,
            String employeeNum,
            String employeeName,
            String departmentName,
            String positionName,
            LocalDate hireDate,
            String employState,
            String bankName,
            String accountNumberEnc,
            String accountHolder) {}
}
