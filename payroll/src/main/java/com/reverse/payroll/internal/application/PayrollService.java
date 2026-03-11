package com.reverse.payroll.internal.application;

import com.reverse.attendance.AttendanceFacade;
import com.reverse.attendance.dto.PayrollAttendanceResponse;
import com.reverse.core.event.EmailSendEvent;
import com.reverse.core.exception.NotFoundException;
import com.reverse.core.exception.UnauthorizedException;
import com.reverse.core.response.PageResponse;
import com.reverse.core.security.FieldCryptoService;
import com.reverse.payroll.internal.domain.InsuranceRate;
import com.reverse.payroll.internal.domain.PayrollLedger;
import com.reverse.payroll.internal.domain.SalarySetting;
import com.reverse.payroll.internal.dto.request.AdminInsuranceRateUpsertRequest;
import com.reverse.payroll.internal.dto.request.AdminSalarySettingUpsertRequest;
import com.reverse.payroll.internal.dto.request.SalaryPasswordCheckRequest;
import com.reverse.payroll.internal.dto.response.AdminInsuranceRateResponse;
import com.reverse.payroll.internal.dto.response.AdminPayrollBatchCalculateFailureResponse;
import com.reverse.payroll.internal.dto.response.AdminPayrollBatchCalculateResponse;
import com.reverse.payroll.internal.dto.response.AdminPayrollFinalizeResponse;
import com.reverse.payroll.internal.dto.response.AdminPayrollLedgerResponse;
import com.reverse.payroll.internal.dto.response.AdminPayrollSendResponse;
import com.reverse.payroll.internal.dto.response.AdminSalarySettingDetailResponse;
import com.reverse.payroll.internal.dto.response.PayrollDetailResponse;
import com.reverse.payroll.internal.dto.response.PayrollListResponse;
import com.reverse.payroll.internal.event.PayrollPayslipSendRequestedEvent;
import com.reverse.payroll.internal.exception.InvalidSalaryPasswordException;
import com.reverse.payroll.internal.infrastructure.PdfGenerator;
import com.reverse.payroll.internal.persistence.PayrollMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PayrollLedger calculateAndSavePayroll(Long employeeId, int year, int month) {
        validateYearMonth(year, month);
        String targetMonth = String.format("%04d-%02d", year, month);

        payrollMapper
                .findPayrollLedgerByYearMonth(employeeId, targetMonth)
                .ifPresent(
                        existingLedger -> {
                            if ("Y".equals(existingLedger.getIsFinalized())) {
                                throw new IllegalStateException("마감된 급여 대장은 재계산할 수 없습니다.");
                            }
                            throw new IllegalStateException("해당 월의 급여 대장이 이미 존재합니다.");
                        });

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

        PayrollAttendanceResponse attendanceInfo =
                attendanceFacade.getAttendanceForPayroll(employeeId, year, month);
        if (attendanceInfo == null) {
            throw new IllegalStateException("근태 정보를 조회할 수 없습니다.");
        }

        BigDecimal baseSalary = salarySetting.getBaseSalary();
        BigDecimal mealAllowance = salarySetting.getMealAllowance();

        BigDecimal hourlyWage = baseSalary.divide(new BigDecimal("209"), 2, RoundingMode.HALF_UP);

        BigDecimal overtimeAmount =
                hourlyWage
                        .multiply(attendanceInfo.getTotalOvertimeHours())
                        .multiply(new BigDecimal("1.5"))
                        .setScale(0, RoundingMode.HALF_UP);

        BigDecimal nightAmount =
                hourlyWage
                        .multiply(attendanceInfo.getNightWorkHours())
                        .multiply(new BigDecimal("0.5"))
                        .setScale(0, RoundingMode.HALF_UP);

        BigDecimal holidayAmount =
                hourlyWage
                        .multiply(attendanceInfo.getHolidayWorkHours())
                        .multiply(new BigDecimal("1.5"))
                        .setScale(0, RoundingMode.HALF_UP);

        BigDecimal totalExtraPayment = overtimeAmount.add(nightAmount).add(holidayAmount);

        BigDecimal dailyWage = baseSalary.divide(new BigDecimal("30"), 0, RoundingMode.HALF_UP);
        BigDecimal absenceDeduction =
                dailyWage
                        .multiply(BigDecimal.valueOf(attendanceInfo.getAbsentDays()))
                        .setScale(0, RoundingMode.HALF_UP);
        BigDecimal unpaidLeaveDeduction =
                dailyWage
                        .multiply(attendanceInfo.getUnpaidLeaveDays())
                        .setScale(0, RoundingMode.HALF_UP);

        BigDecimal totalPayment =
                baseSalary
                        .add(totalExtraPayment)
                        .add(mealAllowance)
                        .subtract(absenceDeduction)
                        .subtract(unpaidLeaveDeduction);

        BigDecimal taxableIncome = totalPayment.subtract(mealAllowance);

        BigDecimal nationalPension =
                taxableIncome
                        .multiply(insuranceRate.getNationalPensionRate())
                        .setScale(0, RoundingMode.HALF_UP);
        BigDecimal healthInsurance =
                taxableIncome
                        .multiply(insuranceRate.getHealthInsuranceRate())
                        .setScale(0, RoundingMode.HALF_UP);
        BigDecimal longTermCare =
                healthInsurance
                        .multiply(insuranceRate.getLongTermCareRate())
                        .setScale(0, RoundingMode.HALF_UP);
        BigDecimal empInsurance =
                taxableIncome
                        .multiply(insuranceRate.getEmpInsuranceRate())
                        .setScale(0, RoundingMode.HALF_UP);

        BigDecimal incomeTax = calculateMonthlyIncomeTax(taxableIncome);
        BigDecimal localTax =
                incomeTax.multiply(new BigDecimal("0.1")).setScale(0, RoundingMode.HALF_UP);

        BigDecimal totalDeduction =
                nationalPension
                        .add(healthInsurance)
                        .add(longTermCare)
                        .add(empInsurance)
                        .add(incomeTax)
                        .add(localTax);
        BigDecimal netPay = totalPayment.subtract(totalDeduction);

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
                        .isFinalized("N")
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

    @Transactional
    public AdminPayrollBatchCalculateResponse calculateMonthlyPayrolls(int year, int month) {
        validateYearMonth(year, month);

        LocalDate targetDate = LocalDate.of(year, month, 1);
        List<Long> employeeIds = payrollMapper.findEmployeeIdsWithSalarySetting(targetDate);
        List<AdminPayrollBatchCalculateFailureResponse> failures = new ArrayList<>();

        int calculatedCount = 0;
        int skippedCount = 0;

        for (Long employeeId : employeeIds) {
            String targetMonth = String.format("%04d-%02d", year, month);
            if (payrollMapper.findPayrollLedgerByYearMonth(employeeId, targetMonth).isPresent()) {
                skippedCount++;
                continue;
            }

            try {
                calculateAndSavePayroll(employeeId, year, month);
                calculatedCount++;
            } catch (RuntimeException e) {
                failures.add(
                        AdminPayrollBatchCalculateFailureResponse.builder()
                                .employeeId(employeeId)
                                .reason(e.getMessage())
                                .build());
            }
        }

        return AdminPayrollBatchCalculateResponse.builder()
                .targetCount(employeeIds.size())
                .calculatedCount(calculatedCount)
                .skippedCount(skippedCount)
                .failedCount(failures.size())
                .failures(failures)
                .build();
    }

    @Transactional
    public AdminPayrollFinalizeResponse finalizeMonthlyPayrolls(int year, int month) {
        validateYearMonth(year, month);
        String targetMonth = String.format("%04d-%02d", year, month);

        int totalCount = payrollMapper.countPayrollLedgersByTargetMonth(targetMonth);
        if (totalCount == 0) {
            throw new NotFoundException("마감할 급여 대장이 없습니다.");
        }

        int alreadyFinalizedCount =
                payrollMapper.countFinalizedPayrollLedgersByTargetMonth(targetMonth);
        int finalizedCount = payrollMapper.finalizePayrollLedgersByTargetMonth(targetMonth);

        return AdminPayrollFinalizeResponse.builder()
                .targetMonth(targetMonth)
                .totalCount(totalCount)
                .finalizedCount(finalizedCount)
                .alreadyFinalizedCount(alreadyFinalizedCount)
                .build();
    }

    public PageResponse<AdminPayrollLedgerResponse> getAdminPayrollLedgers(
            int year,
            int month,
            String employeeName,
            String departmentName,
            String isFinalized,
            int page,
            int size) {
        validateYearMonth(year, month);
        validatePage(page, size);
        String targetMonth = String.format("%04d-%02d", year, month);
        String normalizedFinalized = normalizeFinalizeFlag(isFinalized);
        String normalizedEmployeeName = normalizeKeyword(employeeName);
        String normalizedDepartmentName = normalizeKeyword(departmentName);
        int offset = (page - 1) * size;

        long totalElements =
                payrollMapper.countAdminPayrollLedgersByMonth(
                        targetMonth,
                        normalizedEmployeeName,
                        normalizedDepartmentName,
                        normalizedFinalized);

        List<AdminPayrollLedgerResponse> content =
                payrollMapper
                        .findAdminPayrollLedgersByMonth(
                                targetMonth,
                                normalizedEmployeeName,
                                normalizedDepartmentName,
                                normalizedFinalized,
                                size,
                                offset)
                        .stream()
                        .map(this::toAdminPayrollLedgerResponse)
                        .collect(Collectors.toList());

        return PageResponse.of(content, page, size, totalElements);
    }

    public byte[] exportAdminPayrollLedgersCsv(
            int year, int month, String employeeName, String departmentName, String isFinalized) {
        validateYearMonth(year, month);
        String targetMonth = String.format("%04d-%02d", year, month);
        String normalizedFinalized = normalizeFinalizeFlag(isFinalized);
        String normalizedEmployeeName = normalizeKeyword(employeeName);
        String normalizedDepartmentName = normalizeKeyword(departmentName);

        List<AdminPayrollLedgerResponse> ledgers =
                payrollMapper
                        .findAdminPayrollLedgersByMonth(
                                targetMonth,
                                normalizedEmployeeName,
                                normalizedDepartmentName,
                                normalizedFinalized,
                                Integer.MAX_VALUE,
                                0)
                        .stream()
                        .map(this::toAdminPayrollLedgerResponse)
                        .collect(Collectors.toList());

        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("귀속월,사원ID,사원명,부서,직급,은행명,계좌번호,예금주,기본급,연장수당,식대,총지급액,총공제액,실지급액,마감여부,발송여부\n");

        for (AdminPayrollLedgerResponse ledger : ledgers) {
            csv.append(csvValue(ledger.getTargetMonth()))
                    .append(',')
                    .append(csvValue(ledger.getEmployeeId()))
                    .append(',')
                    .append(csvValue(ledger.getEmployeeName()))
                    .append(',')
                    .append(csvValue(ledger.getDepartmentName()))
                    .append(',')
                    .append(csvValue(ledger.getPositionName()))
                    .append(',')
                    .append(csvValue(ledger.getBankName()))
                    .append(',')
                    .append(csvValue(ledger.getMaskedAccountNumber()))
                    .append(',')
                    .append(csvValue(ledger.getAccountHolder()))
                    .append(',')
                    .append(csvValue(ledger.getSalaryAmount()))
                    .append(',')
                    .append(csvValue(ledger.getOvertimeAmount()))
                    .append(',')
                    .append(csvValue(ledger.getMealAmount()))
                    .append(',')
                    .append(csvValue(ledger.getTotalPayment()))
                    .append(',')
                    .append(csvValue(ledger.getTotalDeductionAmount()))
                    .append(',')
                    .append(csvValue(ledger.getNetPay()))
                    .append(',')
                    .append(csvValue(ledger.getIsFinalized()))
                    .append(',')
                    .append(csvValue(ledger.getIsSent()))
                    .append('\n');
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportBankTransferPayrollLedgersCsv(
            int year, int month, String employeeName, String departmentName, String isFinalized) {
        validateYearMonth(year, month);
        String targetMonth = String.format("%04d-%02d", year, month);
        String normalizedFinalized = "Y";
        String normalizedEmployeeName = normalizeKeyword(employeeName);
        String normalizedDepartmentName = normalizeKeyword(departmentName);

        List<BankTransferPayrollLedgerRow> ledgers =
                payrollMapper
                        .findAdminPayrollLedgersByMonth(
                                targetMonth,
                                normalizedEmployeeName,
                                normalizedDepartmentName,
                                normalizedFinalized,
                                Integer.MAX_VALUE,
                                0)
                        .stream()
                        .map(this::toBankTransferPayrollLedgerRow)
                        .collect(Collectors.toList());

        boolean hasNonFinalizedLedger =
                ledgers.stream().anyMatch(ledger -> !"Y".equals(ledger.isFinalized()));
        if (hasNonFinalizedLedger) {
            throw new IllegalStateException("은행이체용 CSV는 마감된 급여 대장만 다운로드할 수 있습니다.");
        }

        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("귀속월,사원ID,사원명,부서,직급,은행명,계좌번호,예금주,기본급,연장수당,식대,총지급액,총공제액,실지급액,마감여부,발송여부\n");

        for (BankTransferPayrollLedgerRow ledger : ledgers) {
            csv.append(csvValue(ledger.targetMonth()))
                    .append(',')
                    .append(csvValue(ledger.employeeId()))
                    .append(',')
                    .append(csvValue(ledger.employeeName()))
                    .append(',')
                    .append(csvValue(ledger.departmentName()))
                    .append(',')
                    .append(csvValue(ledger.positionName()))
                    .append(',')
                    .append(csvValue(ledger.bankName()))
                    .append(',')
                    .append(csvValue(ledger.accountNumber()))
                    .append(',')
                    .append(csvValue(ledger.accountHolder()))
                    .append(',')
                    .append(csvValue(ledger.salaryAmount()))
                    .append(',')
                    .append(csvValue(ledger.overtimeAmount()))
                    .append(',')
                    .append(csvValue(ledger.mealAmount()))
                    .append(',')
                    .append(csvValue(ledger.totalPayment()))
                    .append(',')
                    .append(csvValue(ledger.totalDeductionAmount()))
                    .append(',')
                    .append(csvValue(ledger.netPay()))
                    .append(',')
                    .append(csvValue(ledger.isFinalized()))
                    .append(',')
                    .append(csvValue(ledger.isSent()))
                    .append('\n');
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public AdminPayrollSendResponse markPayrollLedgerSent(Long ledgerId) {
        PayrollLedger ledger =
                payrollMapper
                        .findPayrollLedgerById(ledgerId)
                        .orElseThrow(() -> new NotFoundException("존재하지 않는 급여 대장입니다."));

        if (!"Y".equals(ledger.getIsFinalized())) {
            throw new IllegalStateException("마감된 급여 대장만 명세서 발송 처리할 수 있습니다.");
        }

        int sentCount = "Y".equals(ledger.getIsSent()) ? 0 : 1;
        int alreadySentCount = "Y".equals(ledger.getIsSent()) ? 1 : 0;

        if (sentCount == 1) {
            eventPublisher.publishEvent(new PayrollPayslipSendRequestedEvent(ledgerId));
        }
        return AdminPayrollSendResponse.builder()
                .ledgerId(ledgerId)
                .targetMonth(ledger.getTargetMonth())
                .sentCount(sentCount)
                .alreadySentCount(alreadySentCount)
                .message("메일 발송 요청을 등록했습니다. 실제 SMTP 발송 결과는 비동기 로그를 확인해야 합니다.")
                .build();
    }

    @Transactional
    public AdminPayrollSendResponse markMonthlyPayrollsSent(int year, int month) {
        validateYearMonth(year, month);
        String targetMonth = String.format("%04d-%02d", year, month);

        int totalCount = payrollMapper.countPayrollLedgersByTargetMonth(targetMonth);
        if (totalCount == 0) {
            throw new NotFoundException("발송 처리할 급여 대장이 없습니다.");
        }

        int finalizedCount = payrollMapper.countFinalizedPayrollLedgersByTargetMonth(targetMonth);
        if (finalizedCount == 0) {
            throw new IllegalStateException("마감된 급여 대장이 없어 명세서를 발송 처리할 수 없습니다.");
        }

        List<PayrollLedger> finalizedLedgers =
                payrollMapper.findAdminPayrollLedgersByMonth(
                        targetMonth, null, null, "Y", Integer.MAX_VALUE, 0);

        int sentCount = 0;
        int alreadySentCount = 0;

        for (PayrollLedger ledger : finalizedLedgers) {
            if ("Y".equals(ledger.getIsSent())) {
                alreadySentCount++;
                continue;
            }
            eventPublisher.publishEvent(new PayrollPayslipSendRequestedEvent(ledger.getId()));
            sentCount++;
        }
        return AdminPayrollSendResponse.builder()
                .targetMonth(targetMonth)
                .sentCount(sentCount)
                .alreadySentCount(alreadySentCount)
                .message("월별 메일 발송 요청을 등록했습니다. 실제 SMTP 발송 결과는 비동기 로그를 확인해야 합니다.")
                .build();
    }

    @Transactional
    public void processPayslipSendRequest(Long ledgerId) {
        PayrollLedger ledger =
                payrollMapper
                        .findPayrollLedgerById(ledgerId)
                        .orElseThrow(() -> new NotFoundException("존재하지 않는 급여 대장입니다."));

        if (!"Y".equals(ledger.getIsFinalized()) || "Y".equals(ledger.getIsSent())) {
            return;
        }

        publishPayslipEmail(ledger);
        payrollMapper.updatePayrollLedgerSent(ledgerId);
    }

    public List<AdminSalarySettingDetailResponse> getSalarySettingHistory(Long employeeId) {
        return payrollMapper.findSalarySettingHistoryByEmployeeId(employeeId).stream()
                .map(
                        setting ->
                                AdminSalarySettingDetailResponse.of(
                                        setting, maskAccountNumber(setting)))
                .collect(Collectors.toList());
    }

    public List<AdminInsuranceRateResponse> getInsuranceRates() {
        return payrollMapper.findAllInsuranceRates().stream()
                .map(AdminInsuranceRateResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminSalarySettingDetailResponse createSalarySetting(
            Long employeeId, AdminSalarySettingUpsertRequest request) {
        validateSalarySettingPeriod(request.getApplyStartDate(), request.getApplyEndDate());
        ensureSalarySettingDoesNotOverlap(
                employeeId, request.getApplyStartDate(), request.getApplyEndDate(), null);

        SalarySetting salarySetting =
                SalarySetting.builder()
                        .employeeId(employeeId)
                        .baseSalary(request.getBaseSalary())
                        .mealAllowance(request.getMealAllowance())
                        .applyStartDate(request.getApplyStartDate())
                        .applyEndDate(request.getApplyEndDate())
                        .build();

        payrollMapper.insertSalarySetting(salarySetting);

        SalarySetting savedSetting =
                payrollMapper
                        .findSalarySettingById(salarySetting.getId())
                        .orElseThrow(() -> new IllegalStateException("급여 설정 저장 후 조회에 실패했습니다."));
        return AdminSalarySettingDetailResponse.of(savedSetting, maskAccountNumber(savedSetting));
    }

    @Transactional
    public AdminSalarySettingDetailResponse updateSalarySetting(
            Long settingId, AdminSalarySettingUpsertRequest request) {
        validateSalarySettingPeriod(request.getApplyStartDate(), request.getApplyEndDate());

        SalarySetting existingSetting =
                payrollMapper
                        .findSalarySettingById(settingId)
                        .orElseThrow(() -> new NotFoundException("존재하지 않는 급여 설정입니다."));

        ensureSalarySettingDoesNotOverlap(
                existingSetting.getEmployeeId(),
                request.getApplyStartDate(),
                request.getApplyEndDate(),
                settingId);

        SalarySetting updatedSetting =
                SalarySetting.builder()
                        .id(settingId)
                        .employeeId(existingSetting.getEmployeeId())
                        .baseSalary(request.getBaseSalary())
                        .mealAllowance(request.getMealAllowance())
                        .applyStartDate(request.getApplyStartDate())
                        .applyEndDate(request.getApplyEndDate())
                        .build();

        int updatedCount = payrollMapper.updateSalarySetting(updatedSetting);
        if (updatedCount == 0) {
            throw new IllegalStateException("급여 설정 수정에 실패했습니다.");
        }

        SalarySetting savedSetting =
                payrollMapper
                        .findSalarySettingById(settingId)
                        .orElseThrow(() -> new IllegalStateException("급여 설정 수정 후 조회에 실패했습니다."));
        return AdminSalarySettingDetailResponse.of(savedSetting, maskAccountNumber(savedSetting));
    }

    @Transactional
    public AdminInsuranceRateResponse createInsuranceRate(AdminInsuranceRateUpsertRequest request) {
        InsuranceRate insuranceRate =
                InsuranceRate.builder()
                        .applyYear(request.getApplyYear())
                        .nationalPensionRate(request.getNationalPensionRate())
                        .healthInsuranceRate(request.getHealthInsuranceRate())
                        .longTermCareRate(request.getLongTermCareRate())
                        .empInsuranceRate(request.getEmpInsuranceRate())
                        .build();

        try {
            payrollMapper.insertInsuranceRate(insuranceRate);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("해당 연도의 요율 정보가 이미 존재합니다.");
        }

        InsuranceRate savedRate =
                payrollMapper
                        .findInsuranceRateById(insuranceRate.getInsuranceId())
                        .orElseThrow(() -> new IllegalStateException("요율 저장 후 조회에 실패했습니다."));
        return AdminInsuranceRateResponse.from(savedRate);
    }

    @Transactional
    public AdminInsuranceRateResponse updateInsuranceRate(
            Long insuranceId, AdminInsuranceRateUpsertRequest request) {
        InsuranceRate existingRate =
                payrollMapper
                        .findInsuranceRateById(insuranceId)
                        .orElseThrow(() -> new NotFoundException("존재하지 않는 요율 설정입니다."));

        InsuranceRate updatedRate =
                InsuranceRate.builder()
                        .insuranceId(insuranceId)
                        .applyYear(request.getApplyYear())
                        .nationalPensionRate(request.getNationalPensionRate())
                        .healthInsuranceRate(request.getHealthInsuranceRate())
                        .longTermCareRate(request.getLongTermCareRate())
                        .empInsuranceRate(request.getEmpInsuranceRate())
                        .build();

        try {
            int updatedCount = payrollMapper.updateInsuranceRate(updatedRate);
            if (updatedCount == 0) {
                throw new IllegalStateException("요율 수정에 실패했습니다.");
            }
        } catch (DuplicateKeyException e) {
            if (existingRate.getApplyYear() != request.getApplyYear()) {
                throw new IllegalArgumentException("해당 연도의 요율 정보가 이미 존재합니다.");
            }
            throw e;
        }

        InsuranceRate savedRate =
                payrollMapper
                        .findInsuranceRateById(insuranceId)
                        .orElseThrow(() -> new IllegalStateException("요율 수정 후 조회에 실패했습니다."));
        return AdminInsuranceRateResponse.from(savedRate);
    }

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

    public List<PayrollListResponse> getRecentPayrollLedgers(Long employeeId, int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit은 1 이상 100 이하여야 합니다.");
        }
        List<PayrollLedger> ledgers =
                payrollMapper.findRecentPayrollLedgersByEmployeeId(employeeId, limit);
        return ledgers.stream().map(PayrollListResponse::from).collect(Collectors.toList());
    }

    public List<PayrollListResponse> getPayrollLedgersByYear(Long employeeId, String year) {
        if (year == null || !year.matches("\\d{4}")) {
            throw new IllegalArgumentException("year는 yyyy 형식이어야 합니다.");
        }
        List<PayrollLedger> ledgers = payrollMapper.findPayrollLedgersByYear(employeeId, year);
        return ledgers.stream().map(PayrollListResponse::from).collect(Collectors.toList());
    }

    public PayrollDetailResponse getPayrollDetail(Long employeeId, Long ledgerId) {
        PayrollLedger ledger =
                payrollMapper
                        .findPayrollLedgerById(ledgerId)
                        .orElseThrow(() -> new NotFoundException("존재하지 않는 급여 명세서입니다."));

        if (!ledger.getEmployeeId().equals(employeeId)) {
            throw new UnauthorizedException("FORBIDDEN", "본인의 급여 명세서만 조회할 수 있습니다.");
        }

        LocalDate targetDate = LocalDate.parse(ledger.getTargetMonth() + "-01");
        SalarySetting salarySetting =
                payrollMapper.findSalarySettingByEmployeeId(employeeId, targetDate).orElse(null);

        String empName =
                ledger.getEmployeeNameSnapshot() != null ? ledger.getEmployeeNameSnapshot() : "사원";
        String deptName =
                ledger.getDeptNameSnapshot() != null ? ledger.getDeptNameSnapshot() : "미소속";
        String posName =
                ledger.getPositionNameSnapshot() != null
                        ? ledger.getPositionNameSnapshot()
                        : "직급없음";

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

    public byte[] getPayslipPdf(Long employeeId, Long ledgerId) {
        PayrollDetailResponse detail = getPayrollDetail(employeeId, ledgerId);

        Map<String, Object> data = new HashMap<>();
        data.put("payroll", detail);

        return pdfGenerator.generatePdfFromHtml("payroll/payslip", data);
    }

    private void validateSalarySettingPeriod(LocalDate applyStartDate, LocalDate applyEndDate) {
        if (applyEndDate != null && applyEndDate.isBefore(applyStartDate)) {
            throw new IllegalArgumentException("적용 종료일은 적용 시작일보다 빠를 수 없습니다.");
        }
    }

    private void validateYearMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("월은 1-12 사이여야 합니다.");
        }
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("유효하지 않은 연도입니다.");
        }
    }

    private void validatePage(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("page는 1 이상이어야 합니다.");
        }
        if (size < 1 || size > 200) {
            throw new IllegalArgumentException("size는 1 이상 200 이하여야 합니다.");
        }
    }

    private BigDecimal calculateMonthlyIncomeTax(BigDecimal monthlyTaxableIncome) {
        if (monthlyTaxableIncome == null || monthlyTaxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal annualTaxableIncome = monthlyTaxableIncome.multiply(new BigDecimal("12"));
        BigDecimal annualIncomeTax = calculateAnnualProgressiveIncomeTax(annualTaxableIncome);
        return annualIncomeTax
                .max(BigDecimal.ZERO)
                .divide(new BigDecimal("12"), 0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAnnualProgressiveIncomeTax(BigDecimal annualTaxableIncome) {
        if (annualTaxableIncome == null || annualTaxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (annualTaxableIncome.compareTo(new BigDecimal("14000000")) <= 0) {
            return annualTaxableIncome.multiply(new BigDecimal("0.06"));
        }
        if (annualTaxableIncome.compareTo(new BigDecimal("50000000")) <= 0) {
            return annualTaxableIncome
                    .multiply(new BigDecimal("0.15"))
                    .subtract(new BigDecimal("1260000"));
        }
        if (annualTaxableIncome.compareTo(new BigDecimal("88000000")) <= 0) {
            return annualTaxableIncome
                    .multiply(new BigDecimal("0.24"))
                    .subtract(new BigDecimal("5760000"));
        }
        if (annualTaxableIncome.compareTo(new BigDecimal("150000000")) <= 0) {
            return annualTaxableIncome
                    .multiply(new BigDecimal("0.35"))
                    .subtract(new BigDecimal("15440000"));
        }
        if (annualTaxableIncome.compareTo(new BigDecimal("300000000")) <= 0) {
            return annualTaxableIncome
                    .multiply(new BigDecimal("0.38"))
                    .subtract(new BigDecimal("19940000"));
        }
        if (annualTaxableIncome.compareTo(new BigDecimal("500000000")) <= 0) {
            return annualTaxableIncome
                    .multiply(new BigDecimal("0.40"))
                    .subtract(new BigDecimal("25940000"));
        }
        if (annualTaxableIncome.compareTo(new BigDecimal("1000000000")) <= 0) {
            return annualTaxableIncome
                    .multiply(new BigDecimal("0.42"))
                    .subtract(new BigDecimal("35940000"));
        }
        return annualTaxableIncome
                .multiply(new BigDecimal("0.45"))
                .subtract(new BigDecimal("65940000"));
    }

    private void ensureSalarySettingDoesNotOverlap(
            Long employeeId, LocalDate applyStartDate, LocalDate applyEndDate, Long excludeId) {
        boolean hasOverlap =
                payrollMapper.existsSalarySettingOverlap(
                        employeeId, applyStartDate, applyEndDate, excludeId);
        if (hasOverlap) {
            throw new IllegalArgumentException("적용 기간이 겹치는 급여 설정이 이미 존재합니다.");
        }
    }

    private AdminPayrollLedgerResponse toAdminPayrollLedgerResponse(PayrollLedger ledger) {
        String accountNumber = decryptAccountNumber(ledger.getAccountNumberSnapshotEnc());

        return AdminPayrollLedgerResponse.builder()
                .id(ledger.getId())
                .employeeId(ledger.getEmployeeId())
                .targetMonth(ledger.getTargetMonth())
                .employeeName(ledger.getEmployeeNameSnapshot())
                .departmentName(ledger.getDeptNameSnapshot())
                .positionName(ledger.getPositionNameSnapshot())
                .bankName(ledger.getBankNameSnapshot())
                .maskedAccountNumber(maskPlainAccountNumber(accountNumber))
                .accountHolder(ledger.getAccountHolderSnapshot())
                .salaryAmount(ledger.getSalaryAmount())
                .overtimeAmount(ledger.getOvertimeAmount())
                .mealAmount(ledger.getMealAmount())
                .totalPayment(ledger.getTotalPayment())
                .totalDeductionAmount(
                        safeAdd(
                                ledger.getNationalPensionAmount(),
                                ledger.getHealthInsuranceAmount(),
                                ledger.getLongTermCareAmount(),
                                ledger.getEmpInsuranceAmount(),
                                ledger.getIncomeTaxAmount(),
                                ledger.getLocalTaxAmount()))
                .netPay(ledger.getNetPay())
                .isFinalized(ledger.getIsFinalized())
                .isSent(ledger.getIsSent())
                .sendStatusDescription("Y".equals(ledger.getIsSent()) ? "발송요청완료" : "미요청")
                .build();
    }

    private BankTransferPayrollLedgerRow toBankTransferPayrollLedgerRow(PayrollLedger ledger) {
        return new BankTransferPayrollLedgerRow(
                ledger.getTargetMonth(),
                ledger.getEmployeeId(),
                ledger.getEmployeeNameSnapshot(),
                ledger.getDeptNameSnapshot(),
                ledger.getPositionNameSnapshot(),
                ledger.getBankNameSnapshot(),
                decryptAccountNumberOrThrow(ledger.getAccountNumberSnapshotEnc(), ledger.getId()),
                ledger.getAccountHolderSnapshot(),
                ledger.getSalaryAmount(),
                ledger.getOvertimeAmount(),
                ledger.getMealAmount(),
                ledger.getTotalPayment(),
                safeAdd(
                        ledger.getNationalPensionAmount(),
                        ledger.getHealthInsuranceAmount(),
                        ledger.getLongTermCareAmount(),
                        ledger.getEmpInsuranceAmount(),
                        ledger.getIncomeTaxAmount(),
                        ledger.getLocalTaxAmount()),
                ledger.getNetPay(),
                ledger.getIsFinalized(),
                ledger.getIsSent());
    }

    private void publishPayslipEmail(PayrollLedger ledger) {
        String email =
                payrollMapper
                        .findEmployeeEmailById(ledger.getEmployeeId())
                        .orElseThrow(() -> new NotFoundException("급여 명세서 수신 이메일이 없습니다."));

        if (email.isBlank()) {
            throw new IllegalStateException("급여 명세서 수신 이메일이 비어 있습니다.");
        }

        String employeeName =
                ledger.getEmployeeNameSnapshot() != null ? ledger.getEmployeeNameSnapshot() : "사원";
        String subject = "[" + ledger.getTargetMonth() + "] 급여 명세서 안내";
        String body =
                """
                <p>%s님,</p>
                <p>%s 급여 명세서가 준비되었습니다.</p>
                <p>시스템에서 급여 명세서를 확인해 주세요.</p>
                """
                        .formatted(employeeName, ledger.getTargetMonth());

        eventPublisher.publishEvent(new EmailSendEvent(email, subject, body));
    }

    private String maskAccountNumber(SalarySetting salarySetting) {
        String accountNumberEnc = salarySetting.getAccountNumberEnc();
        if (accountNumberEnc == null) {
            return null;
        }

        try {
            String plainAccountNumber = fieldCryptoService.decrypt(accountNumberEnc);
            return maskPlainAccountNumber(plainAccountNumber);
        } catch (Exception e) {
            log.warn("급여 설정 계좌번호 마스킹 실패 - employeeId: {}", salarySetting.getEmployeeId(), e);
            return null;
        }
    }

    private String decryptAccountNumber(String accountNumberEnc) {
        if (accountNumberEnc == null || accountNumberEnc.isBlank()) {
            return null;
        }

        try {
            return fieldCryptoService.decrypt(accountNumberEnc);
        } catch (Exception e) {
            log.warn("급여대장 계좌번호 복호화 실패", e);
            return null;
        }
    }

    private String decryptAccountNumberOrThrow(String accountNumberEnc, Long ledgerId) {
        if (accountNumberEnc == null || accountNumberEnc.isBlank()) {
            throw new IllegalStateException("은행이체용 계좌번호가 비어 있습니다. ledgerId=" + ledgerId);
        }

        try {
            return fieldCryptoService.decrypt(accountNumberEnc);
        } catch (Exception e) {
            log.error("은행이체용 계좌번호 복호화 실패 - ledgerId: {}", ledgerId, e);
            throw new IllegalStateException("은행이체용 계좌번호를 복호화할 수 없습니다. ledgerId=" + ledgerId, e);
        }
    }

    private record BankTransferPayrollLedgerRow(
            String targetMonth,
            Long employeeId,
            String employeeName,
            String departmentName,
            String positionName,
            String bankName,
            String accountNumber,
            String accountHolder,
            BigDecimal salaryAmount,
            BigDecimal overtimeAmount,
            BigDecimal mealAmount,
            BigDecimal totalPayment,
            BigDecimal totalDeductionAmount,
            BigDecimal netPay,
            String isFinalized,
            String isSent) {}

    private String maskPlainAccountNumber(String plainAccountNumber) {
        if (plainAccountNumber == null || plainAccountNumber.isBlank()) {
            return null;
        }
        if (plainAccountNumber.length() <= 4) {
            return plainAccountNumber;
        }
        String suffix = plainAccountNumber.substring(plainAccountNumber.length() - 4);
        return "*".repeat(Math.max(0, plainAccountNumber.length() - 4)) + suffix;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeFinalizeFlag(String isFinalized) {
        String normalized = normalizeKeyword(isFinalized);
        if (normalized == null) {
            return null;
        }
        if (!"Y".equals(normalized) && !"N".equals(normalized)) {
            throw new IllegalArgumentException("isFinalized는 Y 또는 N 이어야 합니다.");
        }
        return normalized;
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String text = String.valueOf(value);
        if (!text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        text = text.replace("\"", "\"\"");
        return "\"" + text + "\"";
    }

    private BigDecimal safeAdd(BigDecimal... values) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            if (value != null) {
                sum = sum.add(value);
            }
        }
        return sum;
    }
}
