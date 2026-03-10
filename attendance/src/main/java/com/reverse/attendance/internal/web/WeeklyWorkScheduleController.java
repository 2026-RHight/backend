package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.WeeklyWorkScheduleService;
import com.reverse.attendance.internal.domain.WeeklyWorkSchedule;
import com.reverse.attendance.internal.dto.request.WeeklyWorkScheduleApplyRequest;
import com.reverse.attendance.internal.dto.request.WeeklyWorkScheduleProcessRequest;
import com.reverse.core.security.CustomUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance/weekly")
@RequiredArgsConstructor
public class WeeklyWorkScheduleController {

    private final WeeklyWorkScheduleService scheduleService;

    // 유연근무 신청
    @PostMapping
    public ResponseEntity<String> applySchedule(
            @Valid @RequestBody WeeklyWorkScheduleApplyRequest request,
            @AuthenticationPrincipal CustomUser user) {
        scheduleService.applySchedule(request, user.getEmployeeId());
        return ResponseEntity.ok("유연근무 신청이 완료되었습니다.");
    }

    // 내 신청 내역 조회
    @GetMapping("/my")
    public ResponseEntity<com.reverse.core.response.PageResponse<WeeklyWorkSchedule>>
            getMySchedules(
                    @AuthenticationPrincipal CustomUser user,
                    @RequestParam(defaultValue = "1") int page,
                    @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(scheduleService.getMySchedules(user.getEmployeeId(), page, size));
    }

    // 내 신청 건수 상태별 요약
    @GetMapping("/status-counts")
    public ResponseEntity<com.reverse.attendance.internal.dto.response.RequestStatusCountResponse>
            getMyRequestStatusCounts(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(scheduleService.getMyRequestStatusCounts(user.getEmployeeId()));
    }

    // 신청 취소
    @PutMapping("/{weeklyId}/cancel")
    public ResponseEntity<String> cancelSchedule(
            @PathVariable Long weeklyId, @AuthenticationPrincipal CustomUser user) {
        scheduleService.cancelSchedule(weeklyId, user.getEmployeeId());
        return ResponseEntity.ok("유연근무 신청이 취소되었습니다.");
    }

    // 부서원 신청 내역 조회 (팀장/관리자)
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC')")
    @GetMapping("/team")
    public ResponseEntity<com.reverse.core.response.PageResponse<WeeklyWorkSchedule>>
            getTeamSchedules(
                    @RequestParam(required = false) String status,
                    @RequestParam(defaultValue = "1") int page,
                    @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(scheduleService.getAllSchedules(status, page, size));
    }

    // 결재 처리 (승인/반려 - 팀장/관리자용)
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC')")
    @PutMapping("/process")
    public ResponseEntity<String> processSchedule(
            @RequestBody WeeklyWorkScheduleProcessRequest request) {
        scheduleService.processSchedule(request);
        String result = request.isApprove() ? "승인" : "반려";
        return ResponseEntity.ok("유연근무 신청이 " + result + " 처리되었습니다.");
    }
}
