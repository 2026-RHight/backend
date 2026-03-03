package com.reverse.attendance.internal.web;

import com.reverse.attendance.dto.request.AttendanceModifyRequest;
import com.reverse.attendance.dto.request.ClockInRequest;
import com.reverse.attendance.dto.request.ClockOutRequest;
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

    @PutMapping("/admin/modify")
    public ResponseEntity<String> modifyAttendanceByAdmin(@RequestBody AttendanceModifyRequest request) {
        try {
            // 여기서 @AuthenticationPrincipal 등을 사용해 API 호출한 사람이 ' 팀장 및 관리자 ' 권한인지 체크해야하는 로직들어가야됨.
            attendanceService.modifyAttendanceByAdmin(request);
            return ResponseEntity.ok("근태 기록이 성공적으로 수정되었습니다.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("근태 기록 수정 중 서버 오류가 발생했습니다.");
        }
    }


}