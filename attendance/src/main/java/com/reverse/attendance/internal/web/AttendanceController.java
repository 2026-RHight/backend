package com.reverse.attendance.internal.web;

import com.reverse.attendance.dto.ClockInRequest;
import com.reverse.attendance.dto.ClockOutRequest;
import com.reverse.attendance.internal.application.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<String> clockIn(@RequestBody ClockInRequest request) {
        try {
            Long attendanceId = attendanceService.clockIn(request);
            return ResponseEntity.ok("출근 처리가 완료되었습니다. (기록 ID: " + attendanceId + ")");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("출근 처리 중 서버 오류가 발생했습니다.");
        }
    }

    @PutMapping("/clock-out")
    public ResponseEntity<String> clockOut(@RequestBody ClockOutRequest request) {
        try {
            attendanceService.clockOut(request);
            return ResponseEntity.ok("퇴근 처리가 완료되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("퇴근 처리 중 서버 오류가 발생했습니다.");
        }
    }
}