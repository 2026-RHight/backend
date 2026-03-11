package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.AttendanceAdminService;
import com.reverse.attendance.internal.dto.request.AttendanceMonthlyCloseRequest;
import com.reverse.attendance.internal.dto.request.AttendancePolicyUpsertRequest;
import com.reverse.attendance.internal.dto.response.AdminAttendanceDashboardResponse;
import com.reverse.attendance.internal.dto.response.AdminAttendanceReportResponse;
import com.reverse.attendance.internal.dto.response.AttendanceHistoryResponse;
import com.reverse.attendance.internal.dto.response.AttendanceMonthlyCloseResponse;
import com.reverse.attendance.internal.dto.response.AttendancePolicyResponse;
import com.reverse.core.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(attendanceAdminService.getDashboard(year, month));
    }

    @Operation(summary = "월간 근태 리포트", description = "사원별 월간 근태 집계 리포트를 조회합니다.")
    @GetMapping("/reports")
    @PreAuthorize(ATTENDANCE_REPORT_VIEW_AUTH)
    public ResponseEntity<PageResponse<AdminAttendanceReportResponse>> getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(attendanceAdminService.getMonthlyReport(year, month, page, size));
    }

    @Operation(summary = "근태 이력 조회", description = "월 기준 근태 변경/승인/마감 이력을 조회합니다.")
    @GetMapping("/history")
    @PreAuthorize(ATTENDANCE_REPORT_VIEW_AUTH)
    public ResponseEntity<PageResponse<AttendanceHistoryResponse>> getHistory(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                attendanceAdminService.getHistory(year, month, employeeId, page, size));
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
}
