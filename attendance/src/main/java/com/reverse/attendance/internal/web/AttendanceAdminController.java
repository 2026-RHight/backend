package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.AttendanceAdminService;
import com.reverse.attendance.internal.dto.request.AttendanceMonthlyCloseRequest;
import com.reverse.attendance.internal.dto.request.AttendancePolicyUpsertRequest;
import com.reverse.attendance.internal.dto.response.AdminAttendanceDashboardResponse;
import com.reverse.attendance.internal.dto.response.AdminAttendanceReportResponse;
import com.reverse.attendance.internal.dto.response.AdminDailyAttendanceResponse;
import com.reverse.attendance.internal.dto.response.AttendanceHistoryResponse;
import com.reverse.attendance.internal.dto.response.AttendanceMonthlyCloseResponse;
import com.reverse.attendance.internal.dto.response.AttendancePolicyResponse;
import com.reverse.core.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance/admin")
@RequiredArgsConstructor
public class AttendanceAdminController {

    private static final String ATTENDANCE_POLICY_ADMIN_AUTH =
            "hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC', 'SYSTEM_ADMIN')";
    private static final String ATTENDANCE_REPORT_VIEW_AUTH =
            "hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC', 'HR_ADMIN_PAYROLL', 'SYSTEM_ADMIN')";
    private static final String ATTENDANCE_CLOSING_ADMIN_AUTH =
            "hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_PAYROLL', 'SYSTEM_ADMIN')";

    private final AttendanceAdminService attendanceAdminService;

    @Operation(summary = "근태 규정 조회", description = "관리자가 특정 사원의 근태 규정을 조회합니다.")
    @GetMapping("/policies/{employeeId}")
    @PreAuthorize(ATTENDANCE_POLICY_ADMIN_AUTH)
    public ResponseEntity<AttendancePolicyResponse> getPolicy(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceAdminService.getPolicy(employeeId));
    }

    @Operation(summary = "근태 규정 저장", description = "관리자가 특정 사원의 근태 규정을 신규 등록하거나 수정합니다.")
    @PutMapping("/policies/{employeeId}")
    @PreAuthorize(ATTENDANCE_POLICY_ADMIN_AUTH)
    public ResponseEntity<AttendancePolicyResponse> upsertPolicy(
            @PathVariable Long employeeId,
            @Valid @RequestBody AttendancePolicyUpsertRequest request) {
        return ResponseEntity.ok(attendanceAdminService.upsertPolicy(employeeId, request));
    }

    @Operation(summary = "관리자 근태 대시보드", description = "관리자의 월간 근태 현황과 승인 대기 건수를 조회합니다.")
    @GetMapping("/dashboard")
    @PreAuthorize(ATTENDANCE_REPORT_VIEW_AUTH)
    public ResponseEntity<AdminAttendanceDashboardResponse> getDashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        YearMonth targetMonth = resolveYearMonth(year, month);
        return ResponseEntity.ok(
                attendanceAdminService.getDashboard(
                        targetMonth.getYear(), targetMonth.getMonthValue()));
    }

    @Operation(summary = "월간 근태 리포트", description = "사원별 월간 근태 집계 리포트를 조회합니다.")
    @GetMapping("/reports")
    @PreAuthorize(ATTENDANCE_REPORT_VIEW_AUTH)
    public ResponseEntity<PageResponse<AdminAttendanceReportResponse>> getMonthlyReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        YearMonth targetMonth = resolveYearMonth(year, month);
        return ResponseEntity.ok(
                attendanceAdminService.getMonthlyReport(
                        targetMonth.getYear(), targetMonth.getMonthValue(), page, size));
    }

    @Operation(summary = "관리자 일별 근태 목록 조회", description = "관리자가 기간별 직원 근태 기록 목록을 조회합니다.")
    @GetMapping("/daily-records")
    @PreAuthorize(ATTENDANCE_REPORT_VIEW_AUTH)
    public ResponseEntity<List<AdminDailyAttendanceResponse>> getDailyRecords(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(
                attendanceAdminService.getDailyRecords(startDate, endDate, status));
    }

    @Operation(summary = "근태 이력 조회", description = "월 기준 근태 변경/승인/마감 이력을 조회합니다.")
    @GetMapping("/history")
    @PreAuthorize(ATTENDANCE_REPORT_VIEW_AUTH)
    public ResponseEntity<PageResponse<AttendanceHistoryResponse>> getHistory(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        YearMonth targetMonth = resolveYearMonth(year, month);
        return ResponseEntity.ok(
                attendanceAdminService.getHistory(
                        targetMonth.getYear(),
                        targetMonth.getMonthValue(),
                        employeeId,
                        page,
                        size));
    }

    @Operation(summary = "월 근태 마감", description = "지정한 월의 근태 데이터를 마감 처리합니다.")
    @PostMapping("/monthly-close")
    @PreAuthorize(ATTENDANCE_CLOSING_ADMIN_AUTH)
    public ResponseEntity<AttendanceMonthlyCloseResponse> closeMonth(
            @Valid @RequestBody AttendanceMonthlyCloseRequest request) {
        return ResponseEntity.ok(attendanceAdminService.closeMonth(request));
    }

    @Operation(summary = "월 근태 재오픈", description = "지정한 월의 마감된 근태 데이터를 재오픈합니다.")
    @PostMapping("/monthly-reopen")
    @PreAuthorize(ATTENDANCE_CLOSING_ADMIN_AUTH)
    public ResponseEntity<AttendanceMonthlyCloseResponse> reopenMonth(
            @Valid @RequestBody AttendanceMonthlyCloseRequest request) {
        return ResponseEntity.ok(attendanceAdminService.reopenMonth(request));
    }

    private YearMonth resolveYearMonth(Integer year, Integer month) {
        YearMonth now = YearMonth.now();
        int resolvedYear = year == null ? now.getYear() : year;
        int resolvedMonth = month == null ? now.getMonthValue() : month;
        return YearMonth.of(resolvedYear, resolvedMonth);
    }
}
