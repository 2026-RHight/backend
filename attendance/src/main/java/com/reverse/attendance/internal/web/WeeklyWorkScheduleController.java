package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.WeeklyWorkScheduleService;
import com.reverse.attendance.internal.domain.WeeklyWorkSchedule;
import com.reverse.attendance.internal.dto.request.WeeklyWorkScheduleApplyRequest;
import com.reverse.attendance.internal.dto.request.WeeklyWorkScheduleProcessRequest;
import com.reverse.core.security.CustomUser;
import jakarta.validation.Valid;
import java.util.List;
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
        try {
            scheduleService.applySchedule(request, user.getEmployeeId());
            return ResponseEntity.ok("유연근무 신청이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("유연근무 신청 중 서버 오류가 발생했습니다.");
        }
    }

    // 내 신청 내역 조회
    @GetMapping("/my")
    public ResponseEntity<List<WeeklyWorkSchedule>> getMySchedules(
            @AuthenticationPrincipal CustomUser user) {
        List<WeeklyWorkSchedule> schedules = scheduleService.getMySchedules(user.getEmployeeId());
        return ResponseEntity.ok(schedules);
    }

    // 신청 취소
    @PutMapping("/{weeklyId}/cancel")
    public ResponseEntity<String> cancelSchedule(
            @PathVariable Long weeklyId, @AuthenticationPrincipal CustomUser user) {
        try {
            scheduleService.cancelSchedule(weeklyId, user.getEmployeeId());
            return ResponseEntity.ok("유연근무 신청이 취소되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("취소 처리 중 오류가 발생했습니다.");
        }
    }

    // 부서원 신청 내역 조회 (팀장/관리자)
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC')")
    @GetMapping("/team")
    public ResponseEntity<List<WeeklyWorkSchedule>> getTeamSchedules(
            @RequestParam(required = false) String status) {
        List<WeeklyWorkSchedule> schedules = scheduleService.getAllSchedules(status);
        return ResponseEntity.ok(schedules);
    }

    // 결재 처리 (승인/반려 - 팀장/관리자용)
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC')")
    @PutMapping("/process")
    public ResponseEntity<String> processSchedule(
            @RequestBody WeeklyWorkScheduleProcessRequest request) {
        try {
            scheduleService.processSchedule(request);
            String result = request.isApprove() ? "승인" : "반려";
            return ResponseEntity.ok("유연근무 신청이 " + result + " 처리되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("결재 처리 중 오류가 발생했습니다.");
        }
    }
}
