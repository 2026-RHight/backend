package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.AttendanceService;
import com.reverse.attendance.internal.dto.request.AttendanceModifyRequest;
import com.reverse.attendance.internal.dto.request.ClockInRequest;
import com.reverse.attendance.internal.dto.response.AttendanceRecordResponse;
import com.reverse.attendance.internal.dto.response.AttendanceSummaryResponse;
import com.reverse.core.security.CustomUser;
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

    @PostMapping("/clock-in")
    public ResponseEntity<String> clockIn(
            @Valid @RequestBody ClockInRequest request, @AuthenticationPrincipal CustomUser user) {
        try {
            Long attendanceId = attendanceService.clockIn(request, user.getEmployeeId());
            return ResponseEntity.ok("출근 처리가 완료되었습니다. (기록 ID: " + attendanceId + ")");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("출근 처리 중 서버 오류가 발생했습니다.");
        }
    }

    @PutMapping("/clock-out")
    public ResponseEntity<String> clockOut(@AuthenticationPrincipal CustomUser user) {
        try {
            attendanceService.clockOut(user.getEmployeeId());
            return ResponseEntity.ok("퇴근 처리가 완료되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("퇴근 처리 중 서버 오류가 발생했습니다.");
        }
    }

    @PutMapping("/admin/modify")
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER', 'HR_ADMIN_BASIC')")
    public ResponseEntity<String> modifyAttendanceByAdmin(
            @RequestBody AttendanceModifyRequest request) {
        try {
            // @AuthenticationPrincipal 등을 사용해 API 호출한 사람이 ' 팀장 및 관리자 ' 권한인지 체크해야 하는
            // 로직들어가야됨.
            attendanceService.modifyAttendanceByAdmin(request);
            return ResponseEntity.ok("근태 기록이 성공적으로 수정되었습니다.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("근태 기록 수정 중 서버 오류가 발생했습니다.");
        }
    }

    // 근태 대쉬보드 통계 API
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
