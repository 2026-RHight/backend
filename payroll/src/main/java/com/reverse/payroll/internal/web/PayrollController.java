package com.reverse.payroll.internal.web;

import com.reverse.core.security.CustomUser;
import com.reverse.payroll.internal.application.PayrollService;
import com.reverse.payroll.internal.dto.request.SalaryPasswordCheckRequest;
import com.reverse.payroll.internal.dto.response.PayrollDetailResponse;
import com.reverse.payroll.internal.dto.response.PayrollListResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    // 급여 명세서 조회 전 비밀번호 검증
    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifySalaryPassword(
            @AuthenticationPrincipal CustomUser authUser,
            @Valid @RequestBody SalaryPasswordCheckRequest request) {
        boolean isVerified = payrollService.verifySalaryPassword(authUser.getEmployeeId(), request);
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
            @AuthenticationPrincipal CustomUser authUser, @PathVariable Long ledgerId) {
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
}
