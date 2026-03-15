package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.WeeklyWorkScheduleService;
import com.reverse.attendance.internal.dto.request.WeeklyWorkScheduleApplyRequest;
import com.reverse.attendance.internal.dto.request.WeeklyWorkScheduleProcessRequest;
import com.reverse.attendance.internal.dto.response.TeamWeeklyScheduleOverviewResponse;
import com.reverse.attendance.internal.dto.response.WeeklyWorkScheduleResponse;
import com.reverse.core.response.PageResponse;
import com.reverse.core.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance/weekly")
@RequiredArgsConstructor
public class WeeklyWorkScheduleController {

    private static final String WEEKLY_SCHEDULE_SELF_SERVICE_AUTH =
            "hasAnyRole('EVALUATOR', 'EVALUATEE', 'HR_ADMIN_MASTER', "
                    + "'HR_ADMIN_PAYROLL', 'HR_ADMIN_BASIC', 'SYSTEM_ADMIN')";
    private static final String WEEKLY_SCHEDULE_APPROVER_AUTH =
            "hasAnyRole('EVALUATOR', 'HR_ADMIN_MASTER', 'HR_ADMIN_BASIC', 'SYSTEM_ADMIN')";

    private final WeeklyWorkScheduleService scheduleService;

    // 유연근무 신청
    @Operation(summary = "유연근무 신청", description = "사용자가 새로운 유연근무를 신청합니다.")
    @PostMapping
    @PreAuthorize(WEEKLY_SCHEDULE_SELF_SERVICE_AUTH)
    public ResponseEntity<String> applySchedule(
            @Valid @RequestBody WeeklyWorkScheduleApplyRequest request,
            @AuthenticationPrincipal CustomUser user) {
        scheduleService.applySchedule(request, user.getEmployeeId());
        return ResponseEntity.ok("유연근무 신청이 완료되었습니다.");
    }

    // 내 신청 내역 조회
    @Operation(summary = "내 유연근무 신청 내역 조회", description = "사용자 본인의 유연근무 신청 내역을 조회합니다.")
    @GetMapping("/my")
    @PreAuthorize(WEEKLY_SCHEDULE_SELF_SERVICE_AUTH)
    public ResponseEntity<PageResponse<WeeklyWorkScheduleResponse>> getMySchedules(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(scheduleService.getMySchedules(user.getEmployeeId(), page, size));
    }

    // 내 신청 건수 상태별 요약
    @Operation(summary = "유연근무 상태별 건수 요약", description = "사용자 본인의 유연근무 신청 건수를 상태별로 요약합니다.")
    @GetMapping("/status-counts")
    @PreAuthorize(WEEKLY_SCHEDULE_SELF_SERVICE_AUTH)
    public ResponseEntity<com.reverse.attendance.internal.dto.response.RequestStatusCountResponse>
            getMyRequestStatusCounts(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(scheduleService.getMyRequestStatusCounts(user.getEmployeeId()));
    }

    // 신청 취소
    @Operation(summary = "유연근무 신청 취소", description = "결재 대기 중인 유연근무 신청을 취소합니다.")
    @PutMapping("/{weeklyId}/cancel")
    @PreAuthorize(WEEKLY_SCHEDULE_SELF_SERVICE_AUTH)
    public ResponseEntity<String> cancelSchedule(
            @PathVariable Long weeklyId, @AuthenticationPrincipal CustomUser user) {
        scheduleService.cancelSchedule(weeklyId, user.getEmployeeId());
        return ResponseEntity.ok("유연근무 신청이 취소되었습니다.");
    }

    // 부서원 신청 내역 조회 (팀장/관리자)
    @Operation(summary = "모든 유연근무 신청 내역 조회 (관리자)", description = "관리자가 모든 직원의 유연근무 신청 내역을 조회합니다.")
    @PreAuthorize(WEEKLY_SCHEDULE_APPROVER_AUTH)
    @GetMapping("/team")
    public ResponseEntity<PageResponse<WeeklyWorkScheduleResponse>> getTeamSchedules(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                scheduleService.getTeamSchedules(user.getEmployeeId(), status, page, size));
    }

    @Operation(
            summary = "팀 주간 유연근무 개요 조회",
            description = "관리자가 특정 주의 팀 유연근무 일정과 코어타임 경고 여부를 조회합니다.")
    @PreAuthorize(WEEKLY_SCHEDULE_APPROVER_AUTH)
    @GetMapping("/team-overview")
    public ResponseEntity<TeamWeeklyScheduleOverviewResponse> getTeamWeeklyOverview(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) LocalDate date) {
        return ResponseEntity.ok(scheduleService.getTeamWeeklyOverview(user.getEmployeeId(), date));
    }

    // 결재 처리 (승인/반려 - 팀장/관리자용)
    @Operation(summary = "유연근무 결재 (관리자)", description = "관리자가 직원의 유연근무 신청을 승인하거나 반려합니다.")
    @PreAuthorize(WEEKLY_SCHEDULE_APPROVER_AUTH)
    @PutMapping("/process")
    public ResponseEntity<String> processSchedule(
            @RequestBody WeeklyWorkScheduleProcessRequest request,
            @AuthenticationPrincipal CustomUser user) {
        scheduleService.processSchedule(request, user.getEmployeeId());
        String result = request.isApprove() ? "승인" : "반려";
        return ResponseEntity.ok("유연근무 신청이 " + result + " 처리되었습니다.");
    }
}
