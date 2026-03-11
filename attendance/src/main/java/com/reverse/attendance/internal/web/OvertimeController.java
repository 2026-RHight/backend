package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.OvertimeService;
import com.reverse.attendance.internal.dto.request.OvertimeApplyRequest;
import com.reverse.attendance.internal.dto.request.OvertimeProcessRequest;
import com.reverse.attendance.internal.dto.response.OvertimeResponse;
import com.reverse.core.response.PageResponse;
import com.reverse.core.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/overtimes")
@RequiredArgsConstructor
public class OvertimeController {

    private static final String OVERTIME_SELF_SERVICE_AUTH =
            "hasAnyRole('EVALUATOR', 'EVALUATEE', 'HR_ADMIN_MASTER', "
                    + "'HR_ADMIN_PAYROLL', 'HR_ADMIN_BASIC', 'SYSTEM_ADMIN')";
    private static final String OVERTIME_APPROVER_AUTH =
            "hasAnyRole('EVALUATOR', 'HR_ADMIN_MASTER', 'HR_ADMIN_BASIC', 'SYSTEM_ADMIN')";

    private final OvertimeService overtimeService;

    // 사용자 : 내 신청 내역 조회
    @Operation(summary = "내 연장근무 신청 내역 조회", description = "사용자 본인의 연장근무 신청 내역을 조회합니다.")
    @GetMapping("/my-requests")
    @PreAuthorize(OVERTIME_SELF_SERVICE_AUTH)
    public ResponseEntity<PageResponse<OvertimeResponse>> getMyOvertimes(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(overtimeService.getMyOvertimes(user.getEmployeeId(), page, size));
    }

    // 사용자 : 내 연장근무 신청 건수 상태별 요약
    @Operation(summary = "연장근무 상태별 건수 요약", description = "사용자 본인의 연장근무 신청 건수를 상태별로 요약합니다.")
    @GetMapping("/status-counts")
    @PreAuthorize(OVERTIME_SELF_SERVICE_AUTH)
    public ResponseEntity<com.reverse.attendance.internal.dto.response.RequestStatusCountResponse>
            getMyRequestStatusCounts(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(overtimeService.getMyRequestStatusCounts(user.getEmployeeId()));
    }

    // 사용자 : 연장근무 신청
    @Operation(summary = "연장근무 신청", description = "사용자가 새로운 연장근무를 신청합니다.")
    @PostMapping("/apply")
    @PreAuthorize(OVERTIME_SELF_SERVICE_AUTH)
    public ResponseEntity<String> applyOvertime(
            @Valid @RequestBody OvertimeApplyRequest request,
            @AuthenticationPrincipal CustomUser user) {
        overtimeService.applyOvertime(request, user.getEmployeeId());
        return ResponseEntity.ok("연장근무 신청이 완료되었습니다.");
    }

    // 사용자 : 신청 취소
    @Operation(summary = "연장근무 신청 취소", description = "결재 대기 중인 연장근무 신청을 취소합니다.")
    @PutMapping("/{overtimeId}/cancel")
    @PreAuthorize(OVERTIME_SELF_SERVICE_AUTH)
    public ResponseEntity<String> cancelOvertime(
            @PathVariable Long overtimeId, @AuthenticationPrincipal CustomUser user) {
        overtimeService.cancelOvertime(overtimeId, user.getEmployeeId());
        return ResponseEntity.ok("신청이 취소되었습니다.");
    }

    // 관리자 : 전체 리스트 조회
    @Operation(summary = "모든 연장근무 내역 조회 (관리자)", description = "관리자가 모든 직원의 연장근무 내역을 조회합니다.")
    @PreAuthorize(OVERTIME_APPROVER_AUTH)
    @GetMapping("/admin/requests")
    public ResponseEntity<PageResponse<OvertimeResponse>> getAllOvertimes(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(overtimeService.getAllOvertimes(status, page, size));
    }

    // 관리자 : 승인 / 반려 결재
    @Operation(summary = "연장근무 결재 (관리자)", description = "관리자가 직원의 연장근무 신청을 승인하거나 반려합니다.")
    @PreAuthorize(OVERTIME_APPROVER_AUTH)
    @PutMapping("/admin/process")
    public ResponseEntity<String> processOvertime(@RequestBody OvertimeProcessRequest request) {
        overtimeService.processOvertime(request);
        String message = request.isApprove() ? "승인 처리되었습니다." : "반려 처리되었습니다.";
        return ResponseEntity.ok(message);
    }
}
