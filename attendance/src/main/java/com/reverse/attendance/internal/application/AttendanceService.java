package com.reverse.attendance.internal.application;

import com.reverse.attendance.dto.ClockInRequest;
import com.reverse.attendance.dto.ClockOutRequest;
import com.reverse.attendance.internal.domain.Attendance;
import com.reverse.attendance.internal.domain.AttendanceStatus;
import com.reverse.attendance.internal.persistence.AttendanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceMapper attendanceMapper;

    private static final LocalTime STANDARD_CHECK_IN_TIME = LocalTime.of(9, 0, 0);
    private static final LocalTime STANDARD_CHECK_OUT_TIME = LocalTime.of(18, 0, 0);

    @Transactional
    public Long clockIn(ClockInRequest request) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 1. 중복 출근 검증
        Optional<Attendance> existingRecord = attendanceMapper.findByEmployeeIdAndWorkDate(request.getEmployeeId(), today);
        if (existingRecord.isPresent()) {
            throw new IllegalStateException("이미 오늘의 출근 기록이 존재합니다.");
        }

        AttendanceStatus status = AttendanceStatus.NORMAL;
        if (now.isAfter(STANDARD_CHECK_IN_TIME)) {
            status = AttendanceStatus.TARDY;
            if (request.getTardyReason() == null || request.getTardyReason().trim().isEmpty()) {
                throw new IllegalArgumentException("09:00 이후 출근 시 지각 사유를 반드시 입력해야 합니다.");
            }
        }

        Attendance attendance = Attendance.builder()
                .employeeId(request.getEmployeeId())
                .workDate(today)
                .checkInTime(now)
                .status(status)
                .tardyReason(status == AttendanceStatus.TARDY ? request.getTardyReason() : null)
                .build();

        attendanceMapper.insertCheckIn(attendance);
        return attendance.getAttendanceId();
    }

    @Transactional
    public void clockOut(ClockOutRequest request) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Attendance attendance = attendanceMapper.findByEmployeeIdAndWorkDate(request.getEmployeeId(), today)
                .orElseThrow(() -> new IllegalStateException("오늘의 출근 기록이 존재하지 않아 퇴근 처리를 할 수 없습니다."));

        if (attendance.getCheckOutTime() != null) {
            throw new IllegalStateException("이미 퇴근 처리가 완료되었습니다.");
        }

        AttendanceStatus currentStatus = attendance.getStatus();
        if (now.isBefore(STANDARD_CHECK_OUT_TIME) &&
                (currentStatus == AttendanceStatus.NORMAL || currentStatus == AttendanceStatus.TARDY)) {
            currentStatus = AttendanceStatus.EARLY_LEAVE;
        }

        Attendance updatedAttendance = Attendance.builder()
                .attendanceId(attendance.getAttendanceId())
                .employeeId(attendance.getEmployeeId())
                .workDate(attendance.getWorkDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(now)
                .status(currentStatus)
                .tardyReason(attendance.getTardyReason())
                .modifyReason(attendance.getModifyReason())
                .build();

        attendanceMapper.updateCheckOut(updatedAttendance);
    }
}