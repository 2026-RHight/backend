package com.reverse.attendance.internal.application;

import com.reverse.attendance.dto.request.AttendanceModifyRequest;
import com.reverse.attendance.dto.request.ClockInRequest;
import com.reverse.attendance.dto.request.ClockOutRequest;
import com.reverse.attendance.dto.response.AttendanceRecordResponse;
import com.reverse.attendance.dto.response.AttendanceSummaryResponse;
import com.reverse.attendance.internal.domain.Attendance;
import com.reverse.attendance.internal.domain.enums.AttendanceStatus;
import com.reverse.attendance.internal.persistence.AttendanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Transactional
    public void modifyAttendanceByAdmin(AttendanceModifyRequest request) {

        if (request.getModifyReason() == null || request.getModifyReason().trim().isEmpty()) {
            throw new IllegalArgumentException("근태 기록 수정 시 사유를 반드시 입력해야 합니다.");
        }

        Attendance attendance = attendanceMapper.findByEmployeeIdAndWorkDate(request.getTargetEmployeeId(), request.getWorkDate())
                .orElseThrow(() -> new IllegalStateException("해당 날짜의 근태 기록이 존재하지 않습니다."));

        Attendance updatedAttendance = Attendance.builder()
                .attendanceId(attendance.getAttendanceId())
                .employeeId(attendance.getEmployeeId())
                .workDate(attendance.getWorkDate())
                .checkInTime(request.getNewCheckInTime() != null ? request.getNewCheckInTime() : attendance.getCheckInTime())
                .checkOutTime(request.getNewCheckOutTime() != null ? request.getNewCheckOutTime() : attendance.getCheckOutTime())
                .status(request.getNewStatus() != null ? request.getNewStatus() : attendance.getStatus())
                .tardyReason(attendance.getTardyReason())
                .modifyReason(request.getModifyReason())
                .build();

        attendanceMapper.updateAttendanceByAdmin(updatedAttendance);
    }

    // 월별 통계 대쉬보드
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getMonthlySummary(Long employeeId, int year, int month) {
        String yearMonth = String.format("%04d-%02d", year, month);
        return attendanceMapper.countMonthlySummary(employeeId, yearMonth);
    }

    // 월별 리스트 조회
    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getMonthlyRecords(Long employeeId, int year, int month, String status) {
        String yearMonth = String.format("%04d-%02d", year, month);

        List<Attendance> records = attendanceMapper.findMonthlyRecords(employeeId, yearMonth, status);

        return records.stream()
                .map(record -> AttendanceRecordResponse.builder()
                        .attendanceId(record.getAttendanceId())
                        .workDate(record.getWorkDate())
                        .checkInTime(record.getCheckInTime())
                        .checkOutTime(record.getCheckOutTime())
                        .status(record.getStatus())
                        .statusDescription(record.getStatus().getDescription()) // "정상", "지각" 등 한글 텍스트
                        .build())
                .collect(Collectors.toList());
    }


}