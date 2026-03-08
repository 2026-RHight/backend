package com.reverse.payroll.internal.application;

import com.reverse.attendance.AttendanceFacade;
import com.reverse.attendance.dto.response.PayrollAttendanceResponse;
import com.reverse.core.exception.UnauthorizedException;
import com.reverse.payroll.internal.domain.InsuranceRate;
import com.reverse.payroll.internal.domain.PayrollLedger;
import com.reverse.payroll.internal.domain.SalarySetting;
import com.reverse.payroll.internal.dto.request.SalaryPasswordCheckRequest;
import com.reverse.payroll.internal.dto.response.PayrollDetailResponse;
import com.reverse.payroll.internal.dto.response.PayrollListResponse;
import com.reverse.payroll.internal.exception.InvalidSalaryPasswordException;
import com.reverse.payroll.internal.persistence.PayrollMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayrollService {

    private final PayrollMapper payrollMapper;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceFacade attendanceFacade;

    // 월 급여 계산 및 대장 생성
    @Transactional
    public PayrollLedger calculateAndSavePayroll(Long employeeId, int year, int month) {
        String yearMonth = String.format("%04d-%02d", year, month);

        // 이미 정산된 내역이 있는지 확인
        payrollMapper.findPayrollLedgerByYearMonth(employeeId, yearMonth)
                .ifPresent(ledger -> {
                    throw new IllegalStateException("해당 월의 급여 대장이 이미 존재합니다.");
                });

        // 기본 설정 및 4대보험 요율 적용
        SalarySetting salarySetting = payrollMapper.findSalarySettingByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("급여 기본 설정 정보가 없습니다."));

        InsuranceRate insuranceRate = payrollMapper.findInsuranceRateByApplyYear(year)
                .orElseThrow(() -> new IllegalArgumentException(year + "년도 4대보험 요율 정보가 없습니다."));

        // 근태에서 근무 기록 가져오기
        PayrollAttendanceResponse attendanceInfo = attendanceFacade.getAttendanceForPayroll(employeeId, year, month);

        // 급여 및 수당 계산
        BigDecimal baseSalary = salarySetting.getBaseSalary();
        BigDecimal mealAllowance = salarySetting.getMealAllowance();

        // 통상 임금 기준으로 시급 계산
        BigDecimal hourlyWage = baseSalary.divide(new BigDecimal("209"), 2, RoundingMode.HALF_UP);

        // 연장근무 수당
        BigDecimal overtimeAmount = hourlyWage
                .multiply(BigDecimal.valueOf(attendanceInfo.getTotalOvertimeHours()))
                .multiply(new BigDecimal("1.5"))
                .setScale(0, RoundingMode.HALF_UP);

        BigDecimal totalPayment = baseSalary.add(overtimeAmount).add(mealAllowance);
        BigDecimal taxableIncome = baseSalary.add(overtimeAmount); // 식대를 뺀 과세 기준액

        // 공제 금액(4대보험) 계산
        BigDecimal nationalPension = taxableIncome.multiply(BigDecimal.valueOf(insuranceRate.getNationalPensionRate()))
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal healthInsurance = taxableIncome.multiply(BigDecimal.valueOf(insuranceRate.getHealthInsuranceRate()))
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal longTermCare = healthInsurance.multiply(BigDecimal.valueOf(insuranceRate.getLongTermCareRate()))
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal empInsurance = taxableIncome.multiply(BigDecimal.valueOf(insuranceRate.getEmpInsuranceRate()))
                .setScale(0, RoundingMode.HALF_UP);

        // 소득세
        BigDecimal incomeTax = taxableIncome.multiply(new BigDecimal("0.033")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal localTax = incomeTax.multiply(new BigDecimal("0.1")).setScale(0, RoundingMode.HALF_UP); // 지방소득세 10%

        // 실수령액 계산
        BigDecimal totalDeduction = nationalPension.add(healthInsurance).add(longTermCare)
                .add(empInsurance).add(incomeTax).add(localTax);
        BigDecimal netPay = totalPayment.subtract(totalDeduction);

        // 객체 조립 후 저장
        PayrollLedger ledger = PayrollLedger.builder()
                .employeeId(employeeId)
                .insuranceId(insuranceRate.getInsuranceId())
                .yearMonth(yearMonth)
                .salaryAmount(baseSalary)
                .overtimeAmount(overtimeAmount)
                .mealAmount(mealAllowance)
                .totalPayment(totalPayment)
                .nationalPensionAmount(nationalPension)
                .healthInsuranceAmount(healthInsurance)
                .longTermCareAmount(longTermCare)
                .empInsuranceAmount(empInsurance)
                .incomeTaxAmount(incomeTax)
                .localTaxAmount(localTax)
                .netPay(netPay)
                .isFinalized("Y")
                .isSent("N")
                .build();

        payrollMapper.insertPayrollLedger(ledger);
        return ledger;
    }

    // 급여 명세서 조회를 위한 사용자 비밀번호 검증 (DB의 해시된 비밀번호와 비교)
    public boolean verifySalaryPassword(Long employeeId, SalaryPasswordCheckRequest request) {
        String encodedPassword = payrollMapper.findEmployeePasswordById(employeeId)
                .orElseThrow(() -> new UnauthorizedException("MEMBER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), encodedPassword)) {
            throw new InvalidSalaryPasswordException("비밀번호가 일치하지 않습니다.");
        }
        return true;
    }

    // 최근 6개월 급여 목록 조회
    public List<PayrollListResponse> getRecentPayrollLedgers(Long employeeId, int limit) {
        List<PayrollLedger> ledgers = payrollMapper.findRecentPayrollLedgersByEmployeeId(employeeId, limit);
        return ledgers.stream()
                .map(PayrollListResponse::from)
                .collect(Collectors.toList());
    }

    // 특정 년도의 급여 목록 조회
    public List<PayrollListResponse> getPayrollLedgersByYear(Long employeeId, String year) {
        List<PayrollLedger> ledgers = payrollMapper.findPayrollLedgersByYear(employeeId, year);
        return ledgers.stream()
                .map(PayrollListResponse::from)
                .collect(Collectors.toList());
    }

    // 급여 명세서 상세 조회
    public PayrollDetailResponse getPayrollDetail(Long ledgerId) {
        PayrollLedger ledger = payrollMapper.findPayrollLedgerById(ledgerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 급여 명세서입니다."));
        return PayrollDetailResponse.from(ledger);
    }
}
