package com.reverse.payroll.internal.web;

import com.reverse.core.exception.UnauthorizedException;
import com.reverse.core.response.PageResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.core.security.JwtTokenProvider;
import com.reverse.payroll.internal.application.PayrollService;
import com.reverse.payroll.internal.dto.request.AdminInsuranceRateUpsertRequest;
import com.reverse.payroll.internal.dto.request.AdminSalarySettingUpsertRequest;
import com.reverse.payroll.internal.dto.request.SalaryPasswordCheckRequest;
import com.reverse.payroll.internal.dto.response.AdminInsuranceRateResponse;
import com.reverse.payroll.internal.dto.response.AdminPayrollBatchCalculateResponse;
import com.reverse.payroll.internal.dto.response.AdminPayrollFinalizeResponse;
import com.reverse.payroll.internal.dto.response.AdminPayrollLedgerResponse;
import com.reverse.payroll.internal.dto.response.AdminPayrollSendResponse;
import com.reverse.payroll.internal.dto.response.AdminSalarySettingDetailResponse;
import com.reverse.payroll.internal.dto.response.PayrollDetailResponse;
import com.reverse.payroll.internal.dto.response.PayrollListResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "관리자 급여 기본 설정 이력 조회", description = "특정 사원의 급여 기본 설정 이력과 현재 계좌 정보를 조회합니다.")
    @GetMapping("/admin/salary-settings/{employeeId}")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<List<AdminSalarySettingDetailResponse>> getSalarySettingHistory(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollService.getSalarySettingHistory(employeeId));
    }

    @Operation(summary = "관리자 급여 기본 설정 등록", description = "특정 사원의 급여 기본 설정을 적용기간과 함께 등록합니다.")
    @PostMapping("/admin/salary-settings/{employeeId}")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<AdminSalarySettingDetailResponse> createSalarySetting(
            @PathVariable Long employeeId,
            @Valid @RequestBody AdminSalarySettingUpsertRequest request) {
        return ResponseEntity.ok(payrollService.createSalarySetting(employeeId, request));
    }

    @Operation(summary = "관리자 급여 기본 설정 수정", description = "등록된 급여 기본 설정의 금액 및 적용기간을 수정합니다.")
    @PutMapping("/admin/salary-settings/{settingId}")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<AdminSalarySettingDetailResponse> updateSalarySetting(
            @PathVariable Long settingId,
            @Valid @RequestBody AdminSalarySettingUpsertRequest request) {
        return ResponseEntity.ok(payrollService.updateSalarySetting(settingId, request));
    }

    @Operation(summary = "관리자 4대보험 요율 조회", description = "연도별 4대보험 및 세율 계산용 요율 목록을 조회합니다.")
    @GetMapping("/admin/insurance-rates")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<List<AdminInsuranceRateResponse>> getInsuranceRates() {
        return ResponseEntity.ok(payrollService.getInsuranceRates());
    }

    @Operation(summary = "관리자 4대보험 요율 등록", description = "급여 계산에 사용할 연도별 4대보험 요율을 등록합니다.")
    @PostMapping("/admin/insurance-rates")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<AdminInsuranceRateResponse> createInsuranceRate(
            @Valid @RequestBody AdminInsuranceRateUpsertRequest request) {
        return ResponseEntity.ok(payrollService.createInsuranceRate(request));
    }

    @Operation(summary = "관리자 4대보험 요율 수정", description = "등록된 연도별 4대보험 요율을 수정합니다.")
    @PutMapping("/admin/insurance-rates/{insuranceId}")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<AdminInsuranceRateResponse> updateInsuranceRate(
            @PathVariable Long insuranceId,
            @Valid @RequestBody AdminInsuranceRateUpsertRequest request) {
        return ResponseEntity.ok(payrollService.updateInsuranceRate(insuranceId, request));
    }

    // 급여 명세서 조회 전 비밀번호 검증
    @Operation(summary = "급여 비밀번호 검증", description = "급여 명세서 조회를 위한 2차 인증(비밀번호)을 수행합니다.")
    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifySalaryPassword(
            @AuthenticationPrincipal CustomUser authUser,
            @Valid @RequestBody SalaryPasswordCheckRequest request) {
        boolean isVerified = payrollService.verifySalaryPassword(authUser.getEmployeeId(), request);

        if (isVerified) {
            String token = jwtTokenProvider.createSalaryDetailTicket(authUser.getEmployeeId());
            ResponseCookie cookie =
                    ResponseCookie.from("SALARY_AUTH_TOKEN", token)
                            .httpOnly(true)
                            .path("/api/payroll")
                            .maxAge(300) // 5 minutes
                            .sameSite("Lax")
                            .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(isVerified);
        }

        return ResponseEntity.ok(isVerified);
    }

    // 최근 급여 목록 6개월 조회
    @Operation(summary = "최근 급여 목록 조회", description = "기본 최근 6개월간의 급여 목록을 조회합니다.")
    @GetMapping("/recent")
    public ResponseEntity<List<PayrollListResponse>> getRecentPayrolls(
            @AuthenticationPrincipal CustomUser authUser,
            @RequestParam(defaultValue = "6") int limit) {
        List<PayrollListResponse> response =
                payrollService.getRecentPayrollLedgers(authUser.getEmployeeId(), limit);
        return ResponseEntity.ok(response);
    }

    // 연도별 급여 목록 조회
    @Operation(summary = "연도별 급여 목록 조회", description = "지정된 연도의 모든 급여 목록을 조회합니다.")
    @GetMapping("/year/{year}")
    public ResponseEntity<List<PayrollListResponse>> getPayrollsByYear(
            @AuthenticationPrincipal CustomUser authUser, @PathVariable String year) {
        List<PayrollListResponse> response =
                payrollService.getPayrollLedgersByYear(authUser.getEmployeeId(), year);
        return ResponseEntity.ok(response);
    }

    // 급여 명세서 상세 조회
    @Operation(summary = "급여 명세서 상세 조회", description = "본인의 특정 월 급여 명세서 상세 내역을 조회합니다.")
    @GetMapping("/details/{ledgerId}")
    public ResponseEntity<PayrollDetailResponse> getPayrollDetail(
            @AuthenticationPrincipal CustomUser authUser,
            @PathVariable Long ledgerId,
            @CookieValue(value = "SALARY_AUTH_TOKEN", required = false) String salaryAuthToken) {

        if (salaryAuthToken == null) {
            throw new UnauthorizedException("FORBIDDEN", "급여 명세서 조회를 위한 비밀번호 인증이 필요합니다.");
        }

        // 토큰 유효성 및 소유자 검증
        jwtTokenProvider.validateSalaryDetailTicket(salaryAuthToken, authUser.getEmployeeId());

        PayrollDetailResponse response =
                payrollService.getPayrollDetail(authUser.getEmployeeId(), ledgerId);
        return ResponseEntity.ok(response);
    }

    // 급여 대장 생성 (Admin)
    @Operation(summary = "급여 대장 생성 (Admin)", description = "특정 사원의 지정된 연월 급여 대장을 생성 및 계산합니다.")
    @PostMapping("/calculate/{employeeId}")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<Long> calculateAndSavePayroll(
            @PathVariable Long employeeId, @RequestParam int year, @RequestParam int month) {
        var ledger = payrollService.calculateAndSavePayroll(employeeId, year, month);
        return ResponseEntity.ok(ledger.getId());
    }

    @Operation(
            summary = "월 급여 일괄 계산 (Admin)",
            description = "지정한 귀속월에 대해 급여 설정이 있는 사원들의 급여를 일괄 계산합니다.")
    @PostMapping("/admin/calculate")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<AdminPayrollBatchCalculateResponse> calculateMonthlyPayrolls(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(payrollService.calculateMonthlyPayrolls(year, month));
    }

    @Operation(summary = "월 급여 마감 (Admin)", description = "지정한 귀속월의 급여 대장을 최종 마감 처리합니다.")
    @PostMapping("/admin/finalize")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<AdminPayrollFinalizeResponse> finalizeMonthlyPayrolls(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(payrollService.finalizeMonthlyPayrolls(year, month));
    }

    @Operation(summary = "관리자 급여대장 조회", description = "귀속월 기준으로 급여대장 목록을 페이징 조회합니다.")
    @GetMapping("/admin/ledgers")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<PageResponse<AdminPayrollLedgerResponse>> getAdminPayrollLedgers(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String isFinalized,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                payrollService.getAdminPayrollLedgers(
                        year, month, employeeName, departmentName, isFinalized, page, size));
    }

    @Operation(summary = "관리자 급여대장 CSV 다운로드", description = "귀속월 기준 급여대장 데이터를 CSV로 다운로드합니다.")
    @GetMapping("/admin/ledgers/export")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<byte[]> exportAdminPayrollLedgers(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String isFinalized) {
        byte[] csvBytes =
                payrollService.exportAdminPayrollLedgersCsv(
                        year, month, employeeName, departmentName, isFinalized);
        String filename = "payroll-ledgers-" + year + "-" + String.format("%02d", month) + ".csv";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv"))
                .body(csvBytes);
    }

    @Operation(
            summary = "개별 명세서 발송 처리 (Admin)",
            description = "마감된 급여 대장에 대해 메일 발송 요청을 등록하고 발송 요청 상태를 반영합니다.")
    @PostMapping("/admin/send/{ledgerId}")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<AdminPayrollSendResponse> markPayrollLedgerSent(
            @PathVariable Long ledgerId) {
        return ResponseEntity.ok(payrollService.markPayrollLedgerSent(ledgerId));
    }

    @Operation(
            summary = "월별 명세서 일괄 발송 처리 (Admin)",
            description = "지정한 귀속월의 마감된 급여 대장에 대해 메일 발송 요청을 일괄 등록하고 발송 요청 상태를 반영합니다.")
    @PostMapping("/admin/send")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<AdminPayrollSendResponse> markMonthlyPayrollsSent(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(payrollService.markMonthlyPayrollsSent(year, month));
    }

    // 급여 명세서 다운로드 (PDF)
    @Operation(summary = "급여 명세서 PDF 다운로드", description = "비밀번호 인증 후 급여 명세서를 PDF 형식으로 다운로드합니다.")
    @GetMapping("/download/{ledgerId}")
    public ResponseEntity<byte[]> downloadPayslipPdf(
            @AuthenticationPrincipal CustomUser authUser,
            @PathVariable Long ledgerId,
            @CookieValue(value = "SALARY_AUTH_TOKEN", required = false) String salaryAuthToken) {

        if (salaryAuthToken == null) {
            throw new UnauthorizedException("FORBIDDEN", "명세서 다운로드를 위한 비밀번호 인증이 필요합니다.");
        }
        jwtTokenProvider.validateSalaryDetailTicket(salaryAuthToken, authUser.getEmployeeId());

        byte[] pdfBytes = payrollService.getPayslipPdf(authUser.getEmployeeId(), ledgerId);

        String filename = "payslip_" + ledgerId + ".pdf";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store, private")
                .header("Pragma", "no-cache")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
