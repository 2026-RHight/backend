package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.domain.Attendance;
import com.reverse.attendance.internal.domain.AttendancePolicy;
import com.reverse.attendance.internal.domain.BusinessTrip;
import com.reverse.attendance.internal.domain.LeaveRequest;
import com.reverse.attendance.internal.domain.Overtime;
import com.reverse.attendance.internal.domain.WeeklyWorkSchedule;
import com.reverse.attendance.internal.domain.enums.AttendanceStatus;
import com.reverse.attendance.internal.dto.request.AttendanceModifyRequest;
import com.reverse.attendance.internal.dto.request.ClockInRequest;
import com.reverse.attendance.internal.dto.response.AttendanceCalendarEventResponse;
import com.reverse.attendance.internal.dto.response.AttendanceCalendarResponse;
import com.reverse.attendance.internal.dto.response.AttendanceRecordResponse;
import com.reverse.attendance.internal.dto.response.AttendanceSummaryResponse;
import com.reverse.attendance.internal.dto.response.AttendanceWeeklySummaryResponse;
import com.reverse.attendance.internal.persistence.AttendanceMapper;
import com.reverse.attendance.internal.persistence.AttendancePolicyMapper;
import com.reverse.attendance.internal.persistence.BusinessTripMapper;
import com.reverse.attendance.internal.persistence.OvertimeMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceMapper attendanceMapper;
    private final AttendancePolicyMapper policyMapper; // 💡 사원별 근태 규정 조회를 위해 추가 주입
    private final com.reverse.attendance.internal.persistence.LeaveMapper leaveMapper;
    private final OvertimeMapper overtimeMapper;
    private final BusinessTripMapper businessTripMapper;
    private final com.reverse.attendance.internal.persistence.WeeklyWorkScheduleMapper
            weeklyWorkScheduleMapper;
    private final AttendanceSyncService attendanceSyncService;

    // 기본 출퇴근 시간 (근태 규정이 등록되지 않은 사원을 위한)
    private static final LocalTime FALLBACK_CHECK_IN_TIME = LocalTime.of(9, 0, 0);
    private static final LocalTime FALLBACK_CHECK_OUT_TIME = LocalTime.of(18, 0, 0);

    @Transactional
    public Long clockIn(ClockInRequest request, Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 사원 개인의 근태 규정(출근 시간)을 가져옵니다. 반차 등에 의해 조정될 수 있습니다.
        LocalTime standardCheckInTime = getStandardCheckInTime(employeeId, today);

        AttendanceStatus status = AttendanceStatus.NORMAL;

        // 사원별 기준 시간과 비교함
        if (now.isAfter(standardCheckInTime)) {
            status = AttendanceStatus.TARDY;
            if (request.getTardyReason() == null || request.getTardyReason().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        standardCheckInTime + " 이후 출근 시 지각 사유를 반드시 입력해야 합니다.");
            }
        }

        Attendance attendance =
                Attendance.builder()
                        .employeeId(employeeId)
                        .workDate(today)
                        .checkInTime(now)
                        .status(status)
                        .tardyReason(
                                status == AttendanceStatus.TARDY ? request.getTardyReason() : null)
                        .build();

        try {
            attendanceMapper.insertCheckIn(attendance);
        } catch (DuplicateKeyException e) {
            throw new com.reverse.core.exception.BadRequestException("이미 오늘의 출근 기록이 존재합니다.");
        }
        return attendance.getAttendanceId();
    }

    @Transactional
    public void clockOut(Long employeeId) {
        LocalTime now = LocalTime.now();

        // 기존 기록 조회 (상태 판단을 위해)
        Attendance attendance =
                attendanceMapper
                        .findByEmployeeIdAndWorkDate(employeeId, LocalDate.now())
                        .orElseThrow(
                                () ->
                                        new com.reverse.core.exception.BadRequestException(
                                                "오늘의 출근 기록이 존재하지 않아 퇴근 처리를 할 수 없습니다."));

        if (attendance.getCheckOutTime() != null) {
            throw new com.reverse.core.exception.BadRequestException("이미 퇴근 처리가 완료되었습니다.");
        }
        if (attendance.getCheckInTime() == null) {
            throw new com.reverse.core.exception.BadRequestException(
                    "출근 기록이 없는 상태에서는 퇴근 처리할 수 없습니다.");
        }

        // 사원 개인의 근태 규정(퇴근 시간) 및 조퇴 여부 판단. 반차 등에 의해 조정될 수 있습니다.
        LocalTime standardCheckOutTime = getStandardCheckOutTime(employeeId, LocalDate.now());
        AttendanceStatus currentStatus = attendance.getStatus();

        if (now.isBefore(standardCheckOutTime)
                && (currentStatus == AttendanceStatus.NORMAL
                        || currentStatus == AttendanceStatus.TARDY)) {
            currentStatus = AttendanceStatus.EARLY_LEAVE;
        }

        // 조건부 UPDATE 시도 (다른 스레드가 이미 퇴근 처리했는지 check_out_time IS NULL로 확인)
        Attendance attendanceToUpdate =
                Attendance.builder()
                        .employeeId(employeeId)
                        .workDate(LocalDate.now())
                        .checkOutTime(now)
                        .status(currentStatus)
                        .build();

        int updatedRows = attendanceMapper.updateCheckOut(attendanceToUpdate);

        if (updatedRows == 0) {
            throw new com.reverse.core.exception.BadRequestException("이미 퇴근 처리가 완료되었습니다.");
        }
    }

    @Transactional
    public void modifyAttendanceByAdmin(AttendanceModifyRequest request, Long actorEmployeeId) {

        if (request.getModifyReason() == null || request.getModifyReason().trim().isEmpty()) {
            throw new IllegalArgumentException("근태 기록 수정 시 사유를 반드시 입력해야 합니다.");
        }

        Attendance attendance =
                attendanceMapper
                        .findByEmployeeIdAndWorkDate(
                                request.getTargetEmployeeId(), request.getWorkDate())
                        .orElseThrow(
                                () ->
                                        new com.reverse.core.exception.BadRequestException(
                                                "해당 날짜의 근태 기록이 존재하지 않습니다."));

        if (Boolean.TRUE.equals(attendance.getClosed())) {
            throw new com.reverse.core.exception.BadRequestException("월 마감된 근태 기록은 수정할 수 없습니다.");
        }

        LocalTime resolvedCheckIn = attendance.getCheckInTime();
        if (request.getNewCheckInTime() != null) {
            resolvedCheckIn = request.getNewCheckInTime();
        }

        LocalTime resolvedCheckOut = attendance.getCheckOutTime();
        if (request.getNewCheckOutTime() != null) {
            resolvedCheckOut = request.getNewCheckOutTime();
        }

        AttendanceStatus resolvedStatus = attendance.getStatus();
        if (request.getNewStatus() != null) {
            resolvedStatus = request.getNewStatus();
        }

        String resolvedTardyReason = attendance.getTardyReason();
        if (request.getNewTardyReason() != null) {
            resolvedTardyReason = request.getNewTardyReason();
        }

        if (resolvedCheckIn != null
                && resolvedCheckOut != null
                && resolvedCheckIn.isAfter(resolvedCheckOut)) {
            throw new IllegalArgumentException("출근 시간은 퇴근 시간보다 늦을 수 없습니다.");
        }

        if (resolvedStatus == AttendanceStatus.TARDY
                && (resolvedTardyReason == null || resolvedTardyReason.trim().isEmpty())) {
            throw new IllegalArgumentException("지각 상태로 수정할 때는 지각 사유를 반드시 입력해야 합니다.");
        }

        Attendance updatedAttendance =
                Attendance.builder()
                        .attendanceId(attendance.getAttendanceId())
                        .employeeId(attendance.getEmployeeId())
                        .workDate(attendance.getWorkDate())
                        .checkInTime(resolvedCheckIn)
                        .checkOutTime(resolvedCheckOut)
                        .status(resolvedStatus)
                        .tardyReason(
                                resolvedStatus == AttendanceStatus.TARDY
                                        ? resolvedTardyReason
                                        : null)
                        .modifyReason(request.getModifyReason())
                        .closed(attendance.getClosed())
                        .overtimeHours(attendance.getOvertimeHours())
                        .nightWorkHours(attendance.getNightWorkHours())
                        .holidayWorkHours(attendance.getHolidayWorkHours())
                        .unpaidLeave(attendance.getUnpaidLeave())
                        .build();

        int updatedRows = attendanceMapper.updateAttendanceByAdmin(updatedAttendance);
        if (updatedRows == 0) {
            throw new com.reverse.core.exception.BadRequestException("월 마감된 근태 기록은 수정할 수 없습니다.");
        }
        attendanceSyncService.recordManualChange(
                attendance, updatedAttendance, actorEmployeeId, request.getModifyReason());
    }

    // 월별 통계 대쉬보드
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getMonthlySummary(Long employeeId, int year, int month) {
        String targetMonth = String.format("%04d-%02d", year, month);
        return attendanceMapper.countMonthlySummary(employeeId, targetMonth);
    }

    // 월별 리스트 조회
    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getMonthlyRecords(
            Long employeeId, int year, int month, String status) {
        String targetMonth = String.format("%04d-%02d", year, month);

        List<Attendance> records =
                attendanceMapper.findMonthlyRecords(employeeId, targetMonth, status);

        return records.stream()
                .map(
                        record ->
                                AttendanceRecordResponse.builder()
                                        .attendanceId(record.getAttendanceId())
                                        .workDate(record.getWorkDate())
                                        .checkInTime(record.getCheckInTime())
                                        .checkOutTime(record.getCheckOutTime())
                                        .status(record.getStatus())
                                        .statusDescription(
                                                record.getStatus().getDescription()) // "정상",
                                        // "지각"
                                        // 등
                                        // 한글
                                        // 텍스트
                                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceWeeklySummaryResponse getWeeklySummary(Long employeeId, LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        LocalDate weekStart = targetDate.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        int totalWorkedMinutes =
                attendanceMapper.findRecordsByDateRange(employeeId, weekStart, weekEnd).stream()
                        .mapToInt(this::calculateWorkedMinutes)
                        .sum();

        int standardWeeklyMinutes = 40 * 60;
        int legalMaximumMinutes = 52 * 60;
        int regularWorkedMinutes = Math.min(totalWorkedMinutes, standardWeeklyMinutes);
        int overtimeWorkedMinutes = Math.max(totalWorkedMinutes - standardWeeklyMinutes, 0);
        int progressPercent =
                standardWeeklyMinutes == 0
                        ? 0
                        : (int)
                                Math.round(
                                        (double) totalWorkedMinutes / standardWeeklyMinutes * 100);

        return AttendanceWeeklySummaryResponse.builder()
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .standardWeeklyMinutes(standardWeeklyMinutes)
                .legalMaximumMinutes(legalMaximumMinutes)
                .totalWorkedMinutes(totalWorkedMinutes)
                .regularWorkedMinutes(regularWorkedMinutes)
                .overtimeWorkedMinutes(overtimeWorkedMinutes)
                .progressPercent(progressPercent)
                .weekly52HourExceeded(totalWorkedMinutes > legalMaximumMinutes)
                .weekly52HourWarning(totalWorkedMinutes >= 48 * 60)
                .build();
    }

    @Transactional(readOnly = true)
    public AttendanceCalendarResponse getCalendar(Long employeeId, int year, int month) {
        String targetMonth = String.format("%04d-%02d", year, month);
        List<AttendanceCalendarEventResponse> events = new ArrayList<>();

        attendanceMapper.findMonthlyRecords(employeeId, targetMonth, null).stream()
                .filter(record -> record.getWorkDate() != null)
                .map(this::toAttendanceEvent)
                .forEach(events::add);

        leaveMapper.findLeaveRequestsByEmployeeId(employeeId, 1000, 0).stream()
                .filter(
                        item ->
                                isInMonth(item.getStartDate(), year, month)
                                        || isInMonth(item.getEndDate(), year, month))
                .map(this::toLeaveEvent)
                .forEach(events::add);

        overtimeMapper.findByEmployeeId(employeeId, 1000, 0).stream()
                .filter(item -> isInMonth(item.getWorkDate(), year, month))
                .map(this::toOvertimeEvent)
                .forEach(events::add);

        businessTripMapper.findByEmployeeId(employeeId, 1000, 0).stream()
                .filter(item -> isInMonth(toDate(item.getStartDatetime()), year, month))
                .map(this::toBusinessTripEvent)
                .forEach(events::add);

        weeklyWorkScheduleMapper.findByEmployeeId(employeeId, 1000, 0).stream()
                .filter(item -> isInMonth(item.getPlanDate(), year, month))
                .map(this::toWeeklyScheduleEvent)
                .forEach(events::add);

        events.sort(
                Comparator.comparing(AttendanceCalendarEventResponse::getTargetDate)
                        .thenComparing(AttendanceCalendarEventResponse::getEventId));

        return AttendanceCalendarResponse.builder().year(year).month(month).events(events).build();
    }

    // 💡 내부 헬퍼 메서드: 규정 조회 로직 분리 (가독성을 높이기 위함)
    private LocalTime getStandardCheckInTime(Long employeeId, LocalDate date) {
        java.util.Optional<com.reverse.attendance.internal.domain.WeeklyWorkSchedule>
                approvedSchedule =
                        weeklyWorkScheduleMapper.findApprovedByEmployeeIdAndPlanDate(
                                employeeId, date);
        if (approvedSchedule.isPresent()) {
            return approvedSchedule.get().getStartDate().toLocalTime();
        }

        LocalTime stdTime =
                policyMapper
                        .findByEmployeeId(employeeId)
                        .map(AttendancePolicy::getStdStartTime)
                        .orElse(FALLBACK_CHECK_IN_TIME);

        java.util.Optional<com.reverse.attendance.internal.domain.enums.LeaveType> approvedHalfDay =
                leaveMapper.findApprovedLeaveTypeByDate(employeeId, date);

        if (approvedHalfDay.isPresent()
                && approvedHalfDay.get()
                        == com.reverse.attendance.internal.domain.enums.LeaveType.HALF_AM) {
            // 오전 반차일 경우 출근 기준 시간을 5시간 미룸 (예: 09:00 -> 14:00)
            return stdTime.plusHours(5);
        }
        return stdTime;
    }

    private LocalTime getStandardCheckOutTime(Long employeeId, LocalDate date) {
        java.util.Optional<com.reverse.attendance.internal.domain.WeeklyWorkSchedule>
                approvedSchedule =
                        weeklyWorkScheduleMapper.findApprovedByEmployeeIdAndPlanDate(
                                employeeId, date);
        if (approvedSchedule.isPresent()) {
            return approvedSchedule.get().getEndDate().toLocalTime();
        }

        LocalTime stdTime =
                policyMapper
                        .findByEmployeeId(employeeId)
                        .map(AttendancePolicy::getStdEndTime)
                        .orElse(FALLBACK_CHECK_OUT_TIME);

        java.util.Optional<com.reverse.attendance.internal.domain.enums.LeaveType> approvedHalfDay =
                leaveMapper.findApprovedLeaveTypeByDate(employeeId, date);

        if (approvedHalfDay.isPresent()
                && approvedHalfDay.get()
                        == com.reverse.attendance.internal.domain.enums.LeaveType.HALF_PM) {
            // 오후 반차일 경우 퇴근 기준 시간을 4시간 당김 (예: 18:00 -> 14:00)
            // (점심시간 1시간 제외 고려)
            return stdTime.minusHours(4);
        }
        return stdTime;
    }

    private int calculateWorkedMinutes(Attendance attendance) {
        if (attendance.getCheckInTime() == null || attendance.getCheckOutTime() == null) {
            return 0;
        }
        int baseWorkedMinutes =
                Math.max(
                        0,
                        (attendance.getCheckOutTime().toSecondOfDay()
                                        - attendance.getCheckInTime().toSecondOfDay())
                                / 60);
        return baseWorkedMinutes
                + toMinutes(attendance.getOvertimeHours())
                + toMinutes(attendance.getNightWorkHours())
                + toMinutes(attendance.getHolidayWorkHours());
    }

    private int toMinutes(BigDecimal hours) {
        if (hours == null) {
            return 0;
        }
        return hours.multiply(BigDecimal.valueOf(60)).intValue();
    }

    private AttendanceCalendarEventResponse toAttendanceEvent(Attendance attendance) {
        return AttendanceCalendarEventResponse.builder()
                .eventId("attendance-" + attendance.getAttendanceId())
                .category("ATTENDANCE")
                .title(
                        attendance.getStatus() != null
                                ? attendance.getStatus().getDescription()
                                : "근태 기록")
                .status(attendance.getStatus() != null ? attendance.getStatus().name() : null)
                .targetDate(attendance.getWorkDate())
                .startDateTime(
                        attendance.getCheckInTime() != null
                                ? LocalDateTime.of(
                                        attendance.getWorkDate(), attendance.getCheckInTime())
                                : null)
                .endDateTime(
                        attendance.getCheckOutTime() != null
                                ? LocalDateTime.of(
                                        attendance.getWorkDate(), attendance.getCheckOutTime())
                                : null)
                .memo(
                        attendance.getModifyReason() != null
                                ? attendance.getModifyReason()
                                : attendance.getTardyReason())
                .build();
    }

    private AttendanceCalendarEventResponse toLeaveEvent(LeaveRequest item) {
        return AttendanceCalendarEventResponse.builder()
                .eventId("leave-" + item.getLeaveRequestId())
                .category("LEAVE")
                .title(item.getLeaveType() != null ? item.getLeaveType().name() : "휴가")
                .status(item.getLeaveStatus() != null ? item.getLeaveStatus().name() : null)
                .targetDate(item.getStartDate())
                .startDateTime(
                        item.getStartDate() != null ? item.getStartDate().atStartOfDay() : null)
                .endDateTime(item.getEndDate() != null ? item.getEndDate().atTime(23, 59) : null)
                .memo(item.getReason())
                .build();
    }

    private AttendanceCalendarEventResponse toOvertimeEvent(Overtime item) {
        return AttendanceCalendarEventResponse.builder()
                .eventId("overtime-" + item.getOvertimeId())
                .category("OVERTIME")
                .title("연장근무")
                .status(item.getApprovalStatus() != null ? item.getApprovalStatus().name() : null)
                .targetDate(item.getWorkDate())
                .startDateTime(item.getStartTime())
                .endDateTime(item.getEndTime())
                .memo(item.getReason())
                .build();
    }

    private AttendanceCalendarEventResponse toBusinessTripEvent(BusinessTrip item) {
        return AttendanceCalendarEventResponse.builder()
                .eventId("trip-" + item.getTripId())
                .category("BUSINESS_TRIP")
                .title(item.getTripType() != null ? item.getTripType().name() : "출장")
                .status(item.getApprovalStatus() != null ? item.getApprovalStatus().name() : null)
                .targetDate(toDate(item.getStartDatetime()))
                .startDateTime(item.getStartDatetime())
                .endDateTime(item.getEndDatetime())
                .memo(item.getReason())
                .build();
    }

    private AttendanceCalendarEventResponse toWeeklyScheduleEvent(WeeklyWorkSchedule item) {
        return AttendanceCalendarEventResponse.builder()
                .eventId("weekly-" + item.getWeeklyId())
                .category("WEEKLY_SCHEDULE")
                .title(
                        item.getScheduleTitle() != null
                                ? item.getScheduleTitle()
                                : item.getWorkForm())
                .status(item.getApprovalStatus() != null ? item.getApprovalStatus().name() : null)
                .targetDate(item.getPlanDate())
                .startDateTime(item.getStartDate())
                .endDateTime(item.getEndDate())
                .memo(item.getMemo())
                .build();
    }

    private boolean isInMonth(LocalDate date, int year, int month) {
        return date != null && date.getYear() == year && date.getMonthValue() == month;
    }

    private LocalDate toDate(LocalDateTime value) {
        return value != null ? value.toLocalDate() : null;
    }
}
