package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.AttendanceService;
import com.reverse.attendance.internal.dto.request.AttendanceModifyRequest;
import com.reverse.attendance.internal.dto.request.ClockInRequest;
import com.reverse.attendance.internal.dto.response.AttendanceCalendarResponse;
import com.reverse.attendance.internal.dto.response.AttendanceRecordResponse;
import com.reverse.attendance.internal.dto.response.AttendanceSummaryResponse;
import com.reverse.attendance.internal.dto.response.AttendanceWeeklySummaryResponse;
import com.reverse.core.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
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

    private static final String ATTENDANCE_SELF_SERVICE_AUTH =
            "hasAnyRole('EVALUATOR', 'EVALUATEE', 'HR_ADMIN_MASTER', "
                    + "'HR_ADMIN_PAYROLL', 'HR_ADMIN_BASIC', 'SYSTEM_ADMIN')";
    private static final String ATTENDANCE_OPERATION_ADMIN_AUTH =
            "hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC', 'SYSTEM_ADMIN')";

    private final AttendanceService attendanceService;

    @Operation(summary = "출근 처리", description = "사용자의 출근 기록을 생성합니다.")
    @PostMapping("/clock-in")
    @PreAuthorize(ATTENDANCE_SELF_SERVICE_AUTH)
    public ResponseEntity<String> clockIn(
            @Valid @RequestBody ClockInRequest request, @AuthenticationPrincipal CustomUser user) {
        Long attendanceId = attendanceService.clockIn(request, user.getEmployeeId());
        return ResponseEntity.ok("출근 처리가 완료되었습니다. (기록 ID: " + attendanceId + ")");
    }

    @Operation(summary = "퇴근 처리", description = "사용자의 퇴근 기록을 갱신합니다.")
    @PutMapping("/clock-out")
    @PreAuthorize(ATTENDANCE_SELF_SERVICE_AUTH)
    public ResponseEntity<String> clockOut(@AuthenticationPrincipal CustomUser user) {
        attendanceService.clockOut(user.getEmployeeId());
        return ResponseEntity.ok("퇴근 처리가 완료되었습니다.");
    }

    @Operation(summary = "근태 기록 수정 (관리자)", description = "관리자가 특정 직원의 근태 기록을 수정합니다.")
    @PutMapping("/admin/modify")
    @PreAuthorize(ATTENDANCE_OPERATION_ADMIN_AUTH)
    public ResponseEntity<String> modifyAttendanceByAdmin(
            @Valid @RequestBody AttendanceModifyRequest request,
            @AuthenticationPrincipal CustomUser user) {
        attendanceService.modifyAttendanceByAdmin(request, user.getEmployeeId());
        return ResponseEntity.ok("근태 기록이 성공적으로 수정되었습니다.");
    }

    // 근태 대쉬보드 통계 API
    @Operation(summary = "월간 근태 요약 조회", description = "특정 월의 근태 요약(지각, 결근 일수 등)을 조회합니다.")
    @GetMapping("/summary")
    @PreAuthorize(ATTENDANCE_SELF_SERVICE_AUTH)
    public ResponseEntity<AttendanceSummaryResponse> getMonthlySummary(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        YearMonth targetMonth = resolveYearMonth(year, month);

        AttendanceSummaryResponse summary =
                attendanceService.getMonthlySummary(
                        user.getEmployeeId(), targetMonth.getYear(), targetMonth.getMonthValue());
        return ResponseEntity.ok(summary);
    }

    // 기록 리스트 API (ex. GET
    // /api/v1/attendance/records?year=2026&month=3&status=TARDY)
    @Operation(summary = "월간 근태 기록 리스트 조회", description = "특정 월의 상세 근태 기록 리스트를 조회합니다.")
    @GetMapping("/records")
    @PreAuthorize(ATTENDANCE_SELF_SERVICE_AUTH)
    public ResponseEntity<List<AttendanceRecordResponse>> getMonthlyRecords(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String status) {
        YearMonth targetMonth = resolveYearMonth(year, month);

        List<AttendanceRecordResponse> records =
                attendanceService.getMonthlyRecords(
                        user.getEmployeeId(),
                        targetMonth.getYear(),
                        targetMonth.getMonthValue(),
                        status);
        return ResponseEntity.ok(records);
    }

    @Operation(summary = "주간 근무 요약 조회", description = "특정 날짜가 포함된 주의 근무시간 요약을 조회합니다.")
    @GetMapping("/weekly-summary")
    @PreAuthorize(ATTENDANCE_SELF_SERVICE_AUTH)
    public ResponseEntity<AttendanceWeeklySummaryResponse> getWeeklySummary(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getWeeklySummary(user.getEmployeeId(), date));
    }

    @Operation(summary = "월간 근무 캘린더 조회", description = "특정 월의 근태/휴가/출장/연장근무/유연근무 이벤트를 조회합니다.")
    @GetMapping("/calendar")
    @PreAuthorize(ATTENDANCE_SELF_SERVICE_AUTH)
    public ResponseEntity<AttendanceCalendarResponse> getCalendar(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        YearMonth targetMonth = resolveYearMonth(year, month);
        return ResponseEntity.ok(
                attendanceService.getCalendar(
                        user.getEmployeeId(), targetMonth.getYear(), targetMonth.getMonthValue()));
    }

    private YearMonth resolveYearMonth(Integer year, Integer month) {
        YearMonth now = YearMonth.now();
        int resolvedYear = year == null ? now.getYear() : year;
        int resolvedMonth = month == null ? now.getMonthValue() : month;
        return YearMonth.of(resolvedYear, resolvedMonth);
    }
}
