package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.LeaveService;
import com.reverse.attendance.internal.dto.request.LeaveApplyRequest;
import com.reverse.attendance.internal.dto.request.LeaveProcessRequest;
import com.reverse.attendance.internal.dto.response.LeaveBalanceResponse;
import com.reverse.attendance.internal.domain.LeaveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.reverse.core.security.CustomUser;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    // 나의 휴가 현황 요약 조회 (잔여, 사용, 대기)
    @GetMapping("/balance")
    public ResponseEntity<LeaveBalanceResponse> getMyLeaveBalance(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(user.getEmployeeId()));
    }

    // 내 휴가 신청 내역 리스트 조회
    @GetMapping("/my-requests")
    public ResponseEntity<List<LeaveRequest>> getMyLeaveRequests(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(leaveService.getMyLeaveRequests(user.getEmployeeId()));
    }

    // 휴가 신청
    @PostMapping("/apply")
    public ResponseEntity<String> applyLeave(@Valid @RequestBody LeaveApplyRequest request,
            @AuthenticationPrincipal CustomUser user) {
        try {
            leaveService.applyLeave(request, user.getEmployeeId());
            return ResponseEntity.ok("휴가 신청이 완료되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("휴가 신청 중 오류가 발생했습니다.");
        }
    }

    // 휴가 취소 (결재 대기 상태일 때만 가능)
    @PutMapping("/{leaveRequestId}/cancel")
    public ResponseEntity<String> cancelLeave(
            @PathVariable Long leaveRequestId,
            @AuthenticationPrincipal CustomUser user) {
        try {
            leaveService.cancelLeave(leaveRequestId, user.getEmployeeId());
            return ResponseEntity.ok("휴가 신청이 취소되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("취소 처리 중 오류가 발생했습니다.");
        }
    }

    // 팀원 전체 휴가 리스트 조회(관리자)
    // 예: GET /api/v1/leaves/admin/requests?status=PENDING
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC')")
    @GetMapping("/admin/requests")
    public ResponseEntity<List<LeaveRequest>> getAllTeamLeaveRequests(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(leaveService.getAllTeamLeaveRequests(status));
    }

    // 휴가 승인/반려 결재 처리(관리자)
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC')")
    @PutMapping("/admin/process")
    public ResponseEntity<String> processLeaveRequest(@RequestBody LeaveProcessRequest request) {
        try {
            leaveService.processLeaveRequest(request);
            String message = request.isApprove() ? "휴가가 승인 처리되었습니다." : "휴가가 반려 처리되었습니다.";
            return ResponseEntity.ok(message);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("결재 처리 중 서버 오류가 발생했습니다.");
        }
    }
}