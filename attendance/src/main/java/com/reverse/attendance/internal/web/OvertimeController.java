package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.OvertimeService;
import com.reverse.attendance.internal.domain.Overtime;
import com.reverse.attendance.internal.dto.request.OvertimeApplyRequest;
import com.reverse.attendance.internal.dto.request.OvertimeProcessRequest;
import com.reverse.core.security.CustomUser;
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

    private final OvertimeService overtimeService;

    // 사용자 : 내 신청 내역 조회
    @GetMapping("/my-requests")
    public ResponseEntity<com.reverse.core.response.PageResponse<Overtime>> getMyOvertimes(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(overtimeService.getMyOvertimes(user.getEmployeeId(), page, size));
    }

    // 사용자 : 내 연장근무 신청 건수 상태별 요약
    @GetMapping("/status-counts")
    public ResponseEntity<com.reverse.attendance.internal.dto.response.RequestStatusCountResponse>
            getMyRequestStatusCounts(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(overtimeService.getMyRequestStatusCounts(user.getEmployeeId()));
    }

    // 사용자 : 연장근무 신청
    @PostMapping("/apply")
    public ResponseEntity<String> applyOvertime(
            @Valid @RequestBody OvertimeApplyRequest request,
            @AuthenticationPrincipal CustomUser user) {
        overtimeService.applyOvertime(request, user.getEmployeeId());
        return ResponseEntity.ok("연장근무 신청이 완료되었습니다.");
    }

    // 사용자 : 신청 취소
    @PutMapping("/{overtimeId}/cancel")
    public ResponseEntity<String> cancelOvertime(
            @PathVariable Long overtimeId, @AuthenticationPrincipal CustomUser user) {
        overtimeService.cancelOvertime(overtimeId, user.getEmployeeId());
        return ResponseEntity.ok("신청이 취소되었습니다.");
    }

    // 관리자 : 전체 리스트 조회
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC')")
    @GetMapping("/admin/requests")
    public ResponseEntity<com.reverse.core.response.PageResponse<Overtime>> getAllOvertimes(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(overtimeService.getAllOvertimes(status, page, size));
    }

    // 관리자 : 승인 / 반려 결재
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC')")
    @PutMapping("/admin/process")
    public ResponseEntity<String> processOvertime(@RequestBody OvertimeProcessRequest request) {
        overtimeService.processOvertime(request);
        String message = request.isApprove() ? "승인 처리되었습니다." : "반려 처리되었습니다.";
        return ResponseEntity.ok(message);
    }
}
