package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.application.dto.request.AttendanceModifyRequest;
import com.reverse.attendance.internal.application.dto.request.ClockInRequest;
import com.reverse.attendance.internal.application.dto.request.ClockOutRequest;
import com.reverse.attendance.internal.application.dto.response.AttendanceRecordResponse;
import com.reverse.attendance.internal.application.dto.response.AttendanceSummaryResponse;
import com.reverse.attendance.internal.domain.Attendance;
import com.reverse.attendance.internal.domain.AttendancePolicy;
import com.reverse.attendance.internal.domain.enums.AttendanceStatus;
import com.reverse.attendance.internal.persistence.AttendanceMapper;
import com.reverse.attendance.internal.persistence.AttendancePolicyMapper;
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
    private final AttendancePolicyMapper policyMapper; // 💡 사원별 근태 규정 조회를 위해 추가 주입

    // 기본 출퇴근 시간 (근태 규정이 등록되지 않은 사원을 위한 Fallback)
    private static final LocalTime FALLBACK_CHECK_IN_TIME = LocalTime.of(9, 0, 0);
    private static final LocalTime FALLBACK_CHECK_OUT_TIME = LocalTime.of(18, 0, 0);

    @Transactional
    public Long clockIn(ClockInRequest request) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Optional<Attendance> existingRecord = attendanceMapper.findByEmployeeIdAndWorkDate(request.getEmployeeId(), today);
        if (existingRecord.isPresent()) {
            throw new IllegalStateException("이미 오늘의 출근 기록이 존재합니다.");
        }

        // 사원 개인의 근태 규정(출근 시간)을 가져옵니다.
        LocalTime standardCheckInTime = getStandardCheckInTime(request.getEmployeeId());

        AttendanceStatus status = AttendanceStatus.NORMAL;

        // 사원별 기준 시간과 비교합니다.
        if (now.isAfter(standardCheckInTime)) {
            status = AttendanceStatus.TARDY;
            if (request.getTardyReason() == null || request.getTardyReason().trim().isEmpty()) {
                throw new IllegalArgumentException(standardCheckInTime + " 이후 출근 시 지각 사유를 반드시 입력해야 합니다.");
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
        LocalTime now = LocalTime.now();

        Attendance attendance = attendanceMapper.findByEmployeeIdAndWorkDate(request.getEmployeeId(), LocalDate.now())
                .orElseThrow(() -> new IllegalStateException("오늘의 출근 기록이 존재하지 않아 퇴근 처리를 할 수 없습니다."));

        if (attendance.getCheckOutTime() != null) {
            throw new IllegalStateException("이미 퇴근 처리가 완료되었습니다.");
        }

        // 사원 개인의 근태 규정(퇴근 시간)을 가져옵니다.
        LocalTime standardCheckOutTime = getStandardCheckOutTime(request.getEmployeeId());
        AttendanceStatus currentStatus = attendance.getStatus();

        // 하드코딩된 시간이 아닌, 사원별 기준 시간과 비교하여 조퇴 여부를 판단합니다.
        if (now.isBefore(standardCheckOutTime) &&
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

    // 💡 내부 헬퍼 메서드: 규정 조회 로직 분리 (가독성을 높이기 위함)
    private LocalTime getStandardCheckInTime(Long employeeId) {
        return policyMapper.findByEmployeeId(employeeId)
                .map(AttendancePolicy::getStdStartTime)
                .orElse(FALLBACK_CHECK_IN_TIME);
    }

    private LocalTime getStandardCheckOutTime(Long employeeId) {
        return policyMapper.findByEmployeeId(employeeId)
                .map(AttendancePolicy::getStdEndTime)
                .orElse(FALLBACK_CHECK_OUT_TIME);
    }
}