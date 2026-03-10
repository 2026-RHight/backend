package com.reverse.payroll.internal.web;

import com.reverse.core.exception.UnauthorizedException;
import com.reverse.core.security.CustomUser;
import com.reverse.core.security.JwtTokenProvider;
import com.reverse.payroll.internal.application.PayrollService;
import com.reverse.payroll.internal.dto.request.SalaryPasswordCheckRequest;
import com.reverse.payroll.internal.dto.response.PayrollDetailResponse;
import com.reverse.payroll.internal.dto.response.PayrollListResponse;
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

    // 급여 명세서 조회 전 비밀번호 검증
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
    @GetMapping("/recent")
    public ResponseEntity<List<PayrollListResponse>> getRecentPayrolls(
            @AuthenticationPrincipal CustomUser authUser,
            @RequestParam(defaultValue = "6") int limit) {
        List<PayrollListResponse> response =
                payrollService.getRecentPayrollLedgers(authUser.getEmployeeId(), limit);
        return ResponseEntity.ok(response);
    }

    // 연도별 급여 목록 조회
    @GetMapping("/year/{year}")
    public ResponseEntity<List<PayrollListResponse>> getPayrollsByYear(
            @AuthenticationPrincipal CustomUser authUser, @PathVariable String year) {
        List<PayrollListResponse> response =
                payrollService.getPayrollLedgersByYear(authUser.getEmployeeId(), year);
        return ResponseEntity.ok(response);
    }

    // 급여 명세서 상세 조회
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
    @PostMapping("/calculate/{employeeId}")
    @PreAuthorize("hasRole('HR_ADMIN_PAYROLL')")
    public ResponseEntity<Long> calculateAndSavePayroll(
            @PathVariable Long employeeId, @RequestParam int year, @RequestParam int month) {
        var ledger = payrollService.calculateAndSavePayroll(employeeId, year, month);
        return ResponseEntity.ok(ledger.getId());
    }

    // 급여 명세서 다운로드 (PDF)
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
