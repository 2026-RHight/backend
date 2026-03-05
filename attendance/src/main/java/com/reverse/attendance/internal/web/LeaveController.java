package com.reverse.attendance.internal.web;


import com.reverse.attendance.internal.application.LeaveService;
import com.reverse.attendance.internal.dto.request.LeaveApplyRequest;
import com.reverse.attendance.internal.dto.request.LeaveProcessRequest;
import com.reverse.attendance.internal.dto.response.LeaveBalanceResponse;
import com.reverse.attendance.internal.domain.LeaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    // 잔여 연차 현황 조회
    @GetMapping("/balance")
    public ResponseEntity<LeaveBalanceResponse> getLeaveBalance(@RequestParam Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(employeeId));
    }

    // 휴가 내역 리스트 조회
    @GetMapping("/my-requests")
    public ResponseEntity<List<LeaveRequest>> getMyLeaveRequests(@RequestParam Long employeeId) {
        return ResponseEntity.ok(leaveService.getMyLeaveRequests(employeeId));
    }

    // 휴가 신청
    @PostMapping("/apply")
    public ResponseEntity<String> applyLeave(@RequestBody LeaveApplyRequest request) {
        try {
            leaveService.applyLeave(request);
            return ResponseEntity.ok("휴가 신청이 완료되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("휴가 신청 중 오류가 발생했습니다.");
        }
    }

    // 휴가 신청 취소(대기 상태만)
    @PutMapping("/{leaveRequestId}/cancel")
    public ResponseEntity<String> cancelLeave(
            @PathVariable Long leaveRequestId,
            @RequestParam Long employeeId) {
        try {
            leaveService.cancelLeave(leaveRequestId, employeeId);
            return ResponseEntity.ok("휴가 신청이 취소되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("취소 처리 중 오류가 발생했습니다.");
        }
    }

    // 팀원 전체 휴가 리스트 조회(관리자)
    // 예: GET /api/v1/leaves/admin/requests?status=PENDING
    @GetMapping("/admin/requests")
    public ResponseEntity<List<LeaveRequest>> getAllTeamLeaveRequests(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(leaveService.getAllTeamLeaveRequests(status));
    }

    // 휴가 승인/반려 결재 처리(관리자)
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