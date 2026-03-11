package com.reverse.payroll.internal.application;

import com.reverse.attendance.AttendanceFacade;
import com.reverse.attendance.dto.response.PayrollAttendanceResponse;
import com.reverse.core.exception.UnauthorizedException;
import com.reverse.core.security.FieldCryptoService;
import com.reverse.payroll.internal.domain.InsuranceRate;
import com.reverse.payroll.internal.domain.PayrollLedger;
import com.reverse.payroll.internal.domain.SalarySetting;
import com.reverse.payroll.internal.dto.request.SalaryPasswordCheckRequest;
import com.reverse.payroll.internal.dto.response.PayrollDetailResponse;
import com.reverse.payroll.internal.dto.response.PayrollListResponse;
import com.reverse.payroll.internal.exception.InvalidSalaryPasswordException;
import com.reverse.payroll.internal.infrastructure.PdfGenerator;
import com.reverse.payroll.internal.persistence.PayrollMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayrollService {

    private final PayrollMapper payrollMapper;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceFacade attendanceFacade;
    private final PdfGenerator pdfGenerator;
    private final FieldCryptoService fieldCryptoService;

    /**
     * 특정 사원의 지정된 연도 및 월에 대한 급여 대장을 생성하고 계산합니다.
     *
     * @param employeeId 급여를 계산할 사원의 고유 식별자
     * @param year 대상 연도
     * @param month 대상 월 (1-12)
     * @return 생성된 급여 대장 엔티티(PayrollLedger)
     */
    @Transactional
    public PayrollLedger calculateAndSavePayroll(Long employeeId, int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("월은 1-12 사이여야 합니다.");
        }
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("유효하지 않은 연도입니다.");
        }
        String targetMonth = String.format("%04d-%02d", year, month);

        // 기본 설정 및 4대보험 요율 적용
        LocalDate targetDate = LocalDate.of(year, month, 1);
        SalarySetting salarySetting =
                payrollMapper
                        .findSalarySettingByEmployeeId(employeeId, targetDate)
                        .orElseThrow(() -> new IllegalArgumentException("급여 기본 설정 정보가 없습니다."));

        InsuranceRate insuranceRate =
                payrollMapper
                        .findInsuranceRateByApplyYear(year)
                        .orElseThrow(
                                () -> new IllegalArgumentException(year + "년도 4대보험 요율 정보가 없습니다."));

        // 근태에서 근무 기록 가져오기
        PayrollAttendanceResponse attendanceInfo =
                attendanceFacade.getAttendanceForPayroll(employeeId, year, month);
        if (attendanceInfo == null) {
            throw new IllegalStateException("근태 정보를 조회할 수 없습니다.");
        }

        // 급여 및 수당 계산
        BigDecimal baseSalary = salarySetting.getBaseSalary();
        BigDecimal mealAllowance = salarySetting.getMealAllowance();

        // 1. 수당 계산 (기본급 기반 시급 계산)
        // 월 소정 근로시간 209시간 기준
        BigDecimal hourlyWage = baseSalary.divide(new BigDecimal("209"), 2, RoundingMode.HALF_UP);

        // 연장 수당 (1.5배)
        BigDecimal overtimeAmount =
                hourlyWage
                        .multiply(attendanceInfo.getTotalOvertimeHours())
                        .multiply(new BigDecimal("1.5"))
                        .setScale(0, RoundingMode.HALF_UP);

        // 야간 수당 (별도 0.5배 가산)
        BigDecimal nightAmount =
                hourlyWage
                        .multiply(attendanceInfo.getNightWorkHours())
                        .multiply(new BigDecimal("0.5"))
                        .setScale(0, RoundingMode.HALF_UP);

        // 휴일 수당 (1.5배)
        BigDecimal holidayAmount =
                hourlyWage
                        .multiply(attendanceInfo.getHolidayWorkHours())
                        .multiply(new BigDecimal("1.5"))
                        .setScale(0, RoundingMode.HALF_UP);

        BigDecimal totalExtraPayment = overtimeAmount.add(nightAmount).add(holidayAmount);

        // 2. 차감 계산 (일할 계산)
        // 한달 유급 일수 대략 30일(또는 근무일 20.9일) 기준. 여기서는 20.9시간/8시간 = 26.125일 정도로 잡거나 단순하게 30일
        // 기준.
        // 통상적으로 무급 휴가/결근은 '일급' 기반 차감
        BigDecimal dailyWage = baseSalary.divide(new BigDecimal("30"), 0, RoundingMode.HALF_UP);
        BigDecimal absenceDeduction =
                dailyWage
                        .multiply(BigDecimal.valueOf(attendanceInfo.getAbsentDays()))
                        .setScale(0, RoundingMode.HALF_UP);
        BigDecimal unpaidLeaveDeduction =
                dailyWage
                        .multiply(attendanceInfo.getUnpaidLeaveDays())
                        .setScale(0, RoundingMode.HALF_UP);

        // 총 지급액 = 기본급 + 제수당 + 식대 - (무급분 차감)
        BigDecimal totalPayment =
                baseSalary
                        .add(totalExtraPayment)
                        .add(mealAllowance)
                        .subtract(absenceDeduction)
                        .subtract(unpaidLeaveDeduction);

        // 과세 대상 금액 (식대 제외)
        BigDecimal taxableIncome = totalPayment.subtract(mealAllowance);

        // 공제 금액(4대보험) 계산
        BigDecimal nationalPension =
                taxableIncome
                        .multiply(insuranceRate.getNationalPensionRate())
                        .setScale(0, RoundingMode.HALF_UP);
        BigDecimal healthInsurance =
                taxableIncome
                        .multiply(insuranceRate.getHealthInsuranceRate())
                        .setScale(0, RoundingMode.HALF_UP);
        // 장기요양보험료는 건강보험료의 일정 비율(현재 12.95%)로 계산
        BigDecimal longTermCare =
                healthInsurance
                        .multiply(insuranceRate.getLongTermCareRate())
                        .setScale(0, RoundingMode.HALF_UP);
        BigDecimal empInsurance =
                taxableIncome
                        .multiply(insuranceRate.getEmpInsuranceRate())
                        .setScale(0, RoundingMode.HALF_UP);

        // 소득세 (간이세액표 대신 3.3% placeholder 사용)
        BigDecimal incomeTax =
                taxableIncome.multiply(new BigDecimal("0.033")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal localTax =
                incomeTax
                        .multiply(new BigDecimal("0.1"))
                        .setScale(0, RoundingMode.HALF_UP); // 지방소득세 10%

        // 실수령액 계산
        BigDecimal totalDeduction =
                nationalPension
                        .add(healthInsurance)
                        .add(longTermCare)
                        .add(empInsurance)
                        .add(incomeTax)
                        .add(localTax);
        BigDecimal netPay = totalPayment.subtract(totalDeduction);

        // 사원 정보(이름, 부서 등) 스냅샷 조회
        var empInfo =
                payrollMapper
                        .findEmployeePayslipInfo(employeeId)
                        .orElse(new PayrollMapper.EmployeePayslipInfo("사원", "미소속", "직급없음"));

        PayrollLedger ledger =
                PayrollLedger.builder()
                        .employeeId(employeeId)
                        .insuranceId(insuranceRate.getInsuranceId())
                        .targetMonth(targetMonth)
                        .salaryAmount(baseSalary)
                        .overtimeAmount(totalExtraPayment)
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
                        .employeeNameSnapshot(empInfo.employeeName())
                        .deptNameSnapshot(empInfo.departmentName())
                        .positionNameSnapshot(empInfo.positionName())
                        .bankNameSnapshot(salarySetting.getBankName())
                        .accountNumberSnapshotEnc(salarySetting.getAccountNumberEnc())
                        .accountHolderSnapshot(salarySetting.getAccountHolder())
                        .build();

        try {
            payrollMapper.insertPayrollLedger(ledger);
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("해당 월의 급여 대장이 이미 존재합니다.");
        }
        return ledger;
    }

    /**
     * 급여 명세서 조회를 위한 사용자 비밀번호(2차 인증)를 검증합니다.
     *
     * @param employeeId 비밀번호를 검증할 사원의 고유 식별자
     * @param request 비밀번호 검증 요청 DTO (입력된 비밀번호 포함)
     * @return 검증 성공 여부 (일치하면 true)
     */
    public boolean verifySalaryPassword(Long employeeId, SalaryPasswordCheckRequest request) {
        String encodedPassword =
                payrollMapper
                        .findEmployeePasswordById(employeeId)
                        .orElseThrow(
                                () ->
                                        new UnauthorizedException(
                                                "MEMBER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), encodedPassword)) {
            throw new InvalidSalaryPasswordException("비밀번호가 일치하지 않습니다.");
        }
        return true;
    }

    /**
     * 특정 사원의 최근 n개월 동안의 급여 목록을 조회합니다.
     *
     * @param employeeId 단말 사원의 고유 식별자
     * @param limit 조회할 개월 수 (최대 100)
     * @return 최근 급여 목록을 담은 DTO 리스트
     */
    public List<PayrollListResponse> getRecentPayrollLedgers(Long employeeId, int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit은 1 이상 100 이하여야 합니다.");
        }
        List<PayrollLedger> ledgers =
                payrollMapper.findRecentPayrollLedgersByEmployeeId(employeeId, limit);
        return ledgers.stream().map(PayrollListResponse::from).collect(Collectors.toList());
    }

    /**
     * 특정 사원의 지정된 연도의 급여 목록을 조회합니다.
     *
     * @param employeeId 사원의 고유 식별자
     * @param year 대상 연도 (yyyy 형식)
     * @return 해당 연도의 급여 목록을 담은 DTO 리스트
     */
    public List<PayrollListResponse> getPayrollLedgersByYear(Long employeeId, String year) {
        if (year == null || !year.matches("\\d{4}")) {
            throw new IllegalArgumentException("year는 yyyy 형식이어야 합니다.");
        }
        List<PayrollLedger> ledgers = payrollMapper.findPayrollLedgersByYear(employeeId, year);
        return ledgers.stream().map(PayrollListResponse::from).collect(Collectors.toList());
    }

    /**
     * 급여 명세서의 상세 내역을 조회합니다. 본인 소유의 명세서인지 확인하며, 저장된 스냅샷(부서, 직급, 계좌번호 등)을 우선적으로 사용합니다.
     *
     * @param employeeId 조회하려는 사원의 고유 식별자
     * @param ledgerId 급여 대장의 고유 식별자
     * @return 상세 급여 명세서 응답 DTO
     */
    public PayrollDetailResponse getPayrollDetail(Long employeeId, Long ledgerId) {
        PayrollLedger ledger =
                payrollMapper
                        .findPayrollLedgerById(ledgerId)
                        .orElseThrow(
                                () ->
                                        new com.reverse.core.exception.NotFoundException(
                                                "존재하지 않는 급여 명세서입니다."));

        if (!ledger.getEmployeeId().equals(employeeId)) {
            throw new UnauthorizedException("FORBIDDEN", "본인의 급여 명세서만 조회할 수 있습니다.");
        }

        // 해당 월에 적용되었던 급여 설정을 가져와서 은행 정보 추출
        LocalDate targetDate = LocalDate.parse(ledger.getTargetMonth() + "-01");
        SalarySetting salarySetting =
                payrollMapper.findSalarySettingByEmployeeId(employeeId, targetDate).orElse(null);

        // 사원 정보(이름, 부서 등) - 대장 저장 시점의 스냅샷 정보 사용
        String empName =
                ledger.getEmployeeNameSnapshot() != null ? ledger.getEmployeeNameSnapshot() : "사원";
        String deptName =
                ledger.getDeptNameSnapshot() != null ? ledger.getDeptNameSnapshot() : "미소속";
        String posName =
                ledger.getPositionNameSnapshot() != null
                        ? ledger.getPositionNameSnapshot()
                        : "직급없음";

        // 계좌번호 복호화 (스냅샷 우선 사용)
        String plainAccountNumber = null;
        String targetAccountNumberEnc =
                ledger.getAccountNumberSnapshotEnc() != null
                        ? ledger.getAccountNumberSnapshotEnc()
                        : (salarySetting != null ? salarySetting.getAccountNumberEnc() : null);

        if (targetAccountNumberEnc != null) {
            try {
                plainAccountNumber = fieldCryptoService.decrypt(targetAccountNumberEnc);
            } catch (Exception e) {
                log.error("계좌번호 복호화 실패 - employeeId: {}, ledgerId: {}", employeeId, ledgerId, e);
                throw new IllegalStateException("급여 계좌 정보를 조회할 수 없습니다.", e);
            }
        }

        return PayrollDetailResponse.of(
                ledger, salarySetting, plainAccountNumber, empName, deptName, posName);
    }

    /**
     * 급여 명세서를 PDF 형식으로 생성하여 반환합니다. 내부적으로 HTML 템플릿을 사용하여 데이터를 바인딩한 후 PDF로 변환합니다.
     *
     * @param employeeId 대상 사원의 고유 식별자
     * @param ledgerId 급여 대장의 고유 식별자
     * @return 생성된 PDF 파일의 바이트 배열
     */
    public byte[] getPayslipPdf(Long employeeId, Long ledgerId) {
        PayrollDetailResponse detail = getPayrollDetail(employeeId, ledgerId);

        Map<String, Object> data = new HashMap<>();
        data.put("payroll", detail);

        return pdfGenerator.generatePdfFromHtml("payroll/payslip", data);
    }
}
