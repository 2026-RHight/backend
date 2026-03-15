package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.LeaveService;
import com.reverse.attendance.internal.dto.request.LeaveApplyRequest;
import com.reverse.attendance.internal.dto.request.LeaveProcessRequest;
import com.reverse.attendance.internal.dto.response.LeaveBalanceResponse;
import com.reverse.attendance.internal.dto.response.LeaveGrantHistoryResponse;
import com.reverse.attendance.internal.dto.response.LeaveRequestResponse;
import com.reverse.core.response.PageResponse;
import com.reverse.core.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private static final String LEAVE_SELF_SERVICE_AUTH =
            "hasAnyRole('EVALUATOR', 'EVALUATEE', 'HR_ADMIN_MASTER', "
                    + "'HR_ADMIN_PAYROLL', 'HR_ADMIN_BASIC', 'SYSTEM_ADMIN')";
    private static final String LEAVE_APPROVER_AUTH =
            "hasAnyRole('EVALUATOR', 'HR_ADMIN_MASTER', 'HR_ADMIN_BASIC', 'SYSTEM_ADMIN')";

    private final LeaveService leaveService;

    // 나의 휴가 현황 요약 조회 (잔여, 사용, 대기)
    @Operation(summary = "나의 휴가 현황 요약 조회", description = "사용자의 잔여, 사용, 대기 중인 휴가 현황을 요약하여 조회합니다.")
    @GetMapping("/balance")
    @PreAuthorize(LEAVE_SELF_SERVICE_AUTH)
    public ResponseEntity<LeaveBalanceResponse> getMyLeaveBalance(
            @AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(user.getEmployeeId()));
    }

    @Operation(summary = "나의 연차 부여 내역 조회", description = "사용자 본인의 연차 부여 이력을 조회합니다.")
    @GetMapping("/grants")
    @PreAuthorize(LEAVE_SELF_SERVICE_AUTH)
    public ResponseEntity<List<LeaveGrantHistoryResponse>> getMyLeaveGrantHistory(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(leaveService.getLeaveGrantHistory(user.getEmployeeId(), year));
    }

    // 내 휴가 신청 내역 리스트 조회
    @Operation(summary = "내 휴가 신청 내역 조회", description = "사용자 본인의 휴가 신청 내역을 페이지네이션하여 조회합니다.")
    @GetMapping("/my-requests")
    @PreAuthorize(LEAVE_SELF_SERVICE_AUTH)
    public ResponseEntity<PageResponse<LeaveRequestResponse>> getMyLeaveRequests(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(leaveService.getMyLeaveRequests(user.getEmployeeId(), page, size));
    }

    // 내 휴가 신청 건수 상태별 요약
    @Operation(summary = "내 휴가 상태별 건수 요약", description = "사용자 본인의 휴가 신청 건수를 상태별로 요약하여 조회합니다.")
    @GetMapping("/status-counts")
    @PreAuthorize(LEAVE_SELF_SERVICE_AUTH)
    public ResponseEntity<com.reverse.attendance.internal.dto.response.RequestStatusCountResponse>
            getMyRequestStatusCounts(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(leaveService.getMyRequestStatusCounts(user.getEmployeeId()));
    }

    // 휴가 신청
    @Operation(summary = "휴가 신청", description = "사용자가 새로운 휴가를 신청합니다.")
    @PostMapping("/apply")
    @PreAuthorize(LEAVE_SELF_SERVICE_AUTH)
    public ResponseEntity<String> applyLeave(
            @Valid @RequestBody LeaveApplyRequest request,
            @AuthenticationPrincipal CustomUser user) {
        leaveService.applyLeave(request, user.getEmployeeId());
        return ResponseEntity.ok("휴가 신청이 완료되었습니다.");
    }

    // 휴가 취소 (결재 대기 상태일 때만 가능)
    @Operation(summary = "휴가 신청 취소", description = "결재 대기 중인 본인의 휴가 신청을 취소합니다.")
    @PutMapping("/{leaveRequestId}/cancel")
    @PreAuthorize(LEAVE_SELF_SERVICE_AUTH)
    public ResponseEntity<String> cancelLeave(
            @PathVariable Long leaveRequestId, @AuthenticationPrincipal CustomUser user) {
        leaveService.cancelLeave(leaveRequestId, user.getEmployeeId());
        return ResponseEntity.ok("휴가 신청이 취소되었습니다.");
    }

    // 팀원 전체 휴가 리스트 조회(관리자)
    // 예: GET /api/v1/leaves/admin/requests?status=PENDING
    @Operation(summary = "모든 휴가 신청 내역 조회 (관리자)", description = "관리자가 직원들의 전체 휴가 신청 내역을 조회합니다.")
    @PreAuthorize(LEAVE_APPROVER_AUTH)
    @GetMapping("/admin/requests")
    public ResponseEntity<PageResponse<LeaveRequestResponse>> getAllTeamLeaveRequests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(leaveService.getAllTeamLeaveRequests(status, page, size));
    }

    // 휴가 승인/반려 결재 처리(관리자)
    @Operation(summary = "휴가 결재 (관리자)", description = "관리자가 직원의 휴가 신청을 승인하거나 반려합니다.")
    @PreAuthorize(LEAVE_APPROVER_AUTH)
    @PutMapping("/admin/process")
    public ResponseEntity<String> processLeaveRequest(@RequestBody LeaveProcessRequest request) {
        leaveService.processLeaveRequest(request);
        String message = request.isApprove() ? "휴가가 승인 처리되었습니다." : "휴가가 반려 처리되었습니다.";
        return ResponseEntity.ok(message);
    }
}
