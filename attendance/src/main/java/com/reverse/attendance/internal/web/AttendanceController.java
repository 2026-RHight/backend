package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.AttendanceService;
import com.reverse.attendance.internal.dto.request.AttendanceModifyRequest;
import com.reverse.attendance.internal.dto.request.ClockInRequest;
import com.reverse.attendance.internal.dto.response.AttendanceRecordResponse;
import com.reverse.attendance.internal.dto.response.AttendanceSummaryResponse;
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
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "출근 처리", description = "사용자의 출근 기록을 생성합니다.")
    @PostMapping("/clock-in")
    public ResponseEntity<String> clockIn(
            @Valid @RequestBody ClockInRequest request, @AuthenticationPrincipal CustomUser user) {
        Long attendanceId = attendanceService.clockIn(request, user.getEmployeeId());
        return ResponseEntity.ok("출근 처리가 완료되었습니다. (기록 ID: " + attendanceId + ")");
    }

    @Operation(summary = "퇴근 처리", description = "사용자의 퇴근 기록을 갱신합니다.")
    @PutMapping("/clock-out")
    public ResponseEntity<String> clockOut(@AuthenticationPrincipal CustomUser user) {
        attendanceService.clockOut(user.getEmployeeId());
        return ResponseEntity.ok("퇴근 처리가 완료되었습니다.");
    }

    @Operation(summary = "근태 기록 수정 (관리자)", description = "관리자가 특정 직원의 근태 기록을 수정합니다.")
    @PutMapping("/admin/modify")
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC')")
    public ResponseEntity<String> modifyAttendanceByAdmin(
            @Valid @RequestBody AttendanceModifyRequest request) {
        attendanceService.modifyAttendanceByAdmin(request);
        return ResponseEntity.ok("근태 기록이 성공적으로 수정되었습니다.");
    }

    // 근태 대쉬보드 통계 API
    @Operation(summary = "월간 근태 요약 조회", description = "특정 월의 근태 요약(지각, 결근 일수 등)을 조회합니다.")
    @GetMapping("/summary")
    public ResponseEntity<AttendanceSummaryResponse> getMonthlySummary(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam int year,
            @RequestParam int month) {

        AttendanceSummaryResponse summary =
                attendanceService.getMonthlySummary(user.getEmployeeId(), year, month);
        return ResponseEntity.ok(summary);
    }

    // 기록 리스트 API (ex. GET
    // /api/v1/attendance/records?year=2026&month=3&status=TARDY)
    @Operation(summary = "월간 근태 기록 리스트 조회", description = "특정 월의 상세 근태 기록 리스트를 조회합니다.")
    @GetMapping("/records")
    public ResponseEntity<List<AttendanceRecordResponse>> getMonthlyRecords(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String status) {

        List<AttendanceRecordResponse> records =
                attendanceService.getMonthlyRecords(user.getEmployeeId(), year, month, status);
        return ResponseEntity.ok(records);
    }
}
