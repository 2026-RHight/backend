package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.domain.Attendance;
import com.reverse.attendance.internal.domain.AttendanceHistory;
import com.reverse.attendance.internal.domain.BusinessTrip;
import com.reverse.attendance.internal.domain.LeaveRequest;
import com.reverse.attendance.internal.domain.Overtime;
import com.reverse.attendance.internal.domain.WeeklyWorkSchedule;
import com.reverse.attendance.internal.domain.enums.AttendanceStatus;
import com.reverse.attendance.internal.domain.enums.LeaveType;
import com.reverse.attendance.internal.persistence.AttendanceHistoryMapper;
import com.reverse.attendance.internal.persistence.AttendanceMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceSyncService {

    private final AttendanceMapper attendanceMapper;
    private final AttendanceHistoryMapper attendanceHistoryMapper;

    public void syncApprovedLeave(LeaveRequest leaveRequest) {
        AttendanceStatus statusToSet =
                leaveRequest.getLeaveType() == LeaveType.ANNUAL
                        ? AttendanceStatus.VACATION
                        : AttendanceStatus.HALF_VACATION;

        LocalDate ptr = leaveRequest.getStartDate();
        while (!ptr.isAfter(leaveRequest.getEndDate())) {
            DayOfWeek dayOfWeek = ptr.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                Attendance before =
                        attendanceMapper
                                .findByEmployeeIdAndWorkDate(leaveRequest.getEmployeeId(), ptr)
                                .orElse(null);
                Attendance updated =
                        upsertAttendanceStatus(
                                leaveRequest.getEmployeeId(), ptr, statusToSet, "휴가 승인으로 인한 자동 반영");
                writeHistory(before, updated, null, "LEAVE_APPROVED", "휴가 승인 자동 반영");
            }
            ptr = ptr.plusDays(1);
        }
    }

    public void syncApprovedOvertime(Overtime overtime) {
        BigDecimal overtimeHours =
                BigDecimal.valueOf(
                                java.time.Duration.between(
                                                overtime.getStartTime(), overtime.getEndTime())
                                        .toMinutes())
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        Optional<Attendance> existing =
                attendanceMapper.findByEmployeeIdAndWorkDate(
                        overtime.getEmployeeId(), overtime.getWorkDate());
        existing.ifPresent(this::ensureNotClosed);

        if (existing.isPresent()) {
            attendanceMapper.updateOvertimeHours(
                    overtime.getEmployeeId(),
                    overtime.getWorkDate(),
                    overtimeHours,
                    "연장근무 승인으로 인한 자동 반영");
            Attendance after =
                    attendanceMapper
                            .findByEmployeeIdAndWorkDate(
                                    overtime.getEmployeeId(), overtime.getWorkDate())
                            .orElseThrow();
            writeHistory(existing.get(), after, null, "OVERTIME_APPROVED", "연장근무 승인 자동 반영");
            return;
        }

        Attendance created =
                Attendance.builder()
                        .employeeId(overtime.getEmployeeId())
                        .workDate(overtime.getWorkDate())
                        .status(AttendanceStatus.NORMAL)
                        .modifyReason("연장근무 승인으로 인한 자동 반영")
                        .overtimeHours(overtimeHours)
                        .nightWorkHours(BigDecimal.ZERO)
                        .holidayWorkHours(BigDecimal.ZERO)
                        .unpaidLeave(Boolean.FALSE)
                        .closed(Boolean.FALSE)
                        .build();
        attendanceMapper.insertOrUpdateAttendance(created);
        Attendance after =
                attendanceMapper
                        .findByEmployeeIdAndWorkDate(
                                overtime.getEmployeeId(), overtime.getWorkDate())
                        .orElseThrow();
        writeHistory(null, after, null, "OVERTIME_APPROVED", "연장근무 승인 자동 반영");
    }

    public void syncApprovedBusinessTrip(BusinessTrip trip) {
        LocalDate cursor = trip.getStartDatetime().toLocalDate();
        LocalDate endDate = trip.getEndDatetime().toLocalDate();

        while (!cursor.isAfter(endDate)) {
            DayOfWeek dayOfWeek = cursor.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                Attendance before =
                        attendanceMapper
                                .findByEmployeeIdAndWorkDate(trip.getEmployeeId(), cursor)
                                .orElse(null);
                Attendance updated =
                        upsertAttendanceStatus(
                                trip.getEmployeeId(),
                                cursor,
                                AttendanceStatus.BUSINESS_TRIP,
                                "출장 승인으로 인한 자동 반영");
                writeHistory(before, updated, null, "BUSINESS_TRIP_APPROVED", "출장 승인 자동 반영");
            }
            cursor = cursor.plusDays(1);
        }
    }

    public void recordManualChange(
            Attendance before, Attendance after, Long actorEmployeeId, String reason) {
        writeHistory(before, after, actorEmployeeId, "ADMIN_MODIFY", reason);
    }

    public void recordApprovedWeeklySchedule(WeeklyWorkSchedule schedule) {
        attendanceHistoryMapper.insertHistory(
                AttendanceHistory.builder()
                        .attendanceId(null)
                        .employeeId(schedule.getEmployeeId())
                        .actorEmployeeId(null)
                        .actionType("WEEKLY_SCHEDULE_APPROVED")
                        .reason(
                                String.format(
                                        "유연근무 승인 반영 [%s] %s",
                                        schedule.getWorkForm(), schedule.getScheduleTitle()))
                        .workDate(schedule.getPlanDate())
                        .beforeClosed(null)
                        .afterClosed(null)
                        .build());
    }

    public int autoCloseMissingCheckOutsByDate(
            LocalDate workDate, String reason, String actionType) {
        List<Attendance> targets = attendanceMapper.findOpenAttendancesByDate(workDate);
        for (Attendance before : targets) {
            Attendance after = autoCloseAttendance(before, reason);
            writeHistory(before, after, null, actionType, reason);
        }
        return targets.size();
    }

    public int autoCloseMissingCheckOutsByMonth(
            String targetMonth, String reason, String actionType) {
        List<Attendance> targets = attendanceMapper.findOpenAttendancesByMonth(targetMonth);
        for (Attendance before : targets) {
            Attendance after = autoCloseAttendance(before, reason);
            writeHistory(before, after, null, actionType, reason);
        }
        return targets.size();
    }

    public int recordCloseStateChanges(
            List<Attendance> beforeList,
            String targetMonth,
            boolean beforeClosed,
            boolean afterClosed,
            String actionType,
            String reason) {
        Map<Long, Attendance> beforeById = new HashMap<>();
        for (Attendance attendance : beforeList) {
            if (Boolean.valueOf(beforeClosed).equals(attendance.getClosed())) {
                beforeById.put(attendance.getAttendanceId(), attendance);
            }
        }

        if (beforeById.isEmpty()) {
            return 0;
        }

        List<Attendance> afterList = attendanceMapper.findAttendancesByMonth(targetMonth);
        int historyCount = 0;
        for (Attendance after : afterList) {
            Attendance before = beforeById.get(after.getAttendanceId());
            if (before == null) {
                continue;
            }
            if (Boolean.valueOf(afterClosed).equals(after.getClosed())) {
                writeHistory(before, after, null, actionType, reason);
                historyCount++;
            }
        }
        return historyCount;
    }

    private Attendance upsertAttendanceStatus(
            Long employeeId, LocalDate workDate, AttendanceStatus status, String reason) {
        Optional<Attendance> existing =
                attendanceMapper.findByEmployeeIdAndWorkDate(employeeId, workDate);
        existing.ifPresent(this::ensureNotClosed);

        Attendance source = existing.orElse(null);
        Attendance target =
                Attendance.builder()
                        .attendanceId(source != null ? source.getAttendanceId() : null)
                        .employeeId(employeeId)
                        .workDate(workDate)
                        .checkInTime(source != null ? source.getCheckInTime() : null)
                        .checkOutTime(source != null ? source.getCheckOutTime() : null)
                        .status(status)
                        .tardyReason(source != null ? source.getTardyReason() : null)
                        .modifyReason(reason)
                        .closed(Boolean.FALSE)
                        .overtimeHours(source != null ? source.getOvertimeHours() : BigDecimal.ZERO)
                        .nightWorkHours(
                                source != null ? source.getNightWorkHours() : BigDecimal.ZERO)
                        .holidayWorkHours(
                                source != null ? source.getHolidayWorkHours() : BigDecimal.ZERO)
                        .unpaidLeave(source != null ? source.getUnpaidLeave() : Boolean.FALSE)
                        .build();
        attendanceMapper.insertOrUpdateAttendance(target);
        return attendanceMapper.findByEmployeeIdAndWorkDate(employeeId, workDate).orElseThrow();
    }

    private Attendance autoCloseAttendance(Attendance before, String reason) {
        ensureNotClosed(before);

        Attendance target =
                Attendance.builder()
                        .attendanceId(before.getAttendanceId())
                        .employeeId(before.getEmployeeId())
                        .workDate(before.getWorkDate())
                        .checkInTime(before.getCheckInTime())
                        .checkOutTime(before.getCheckOutTime())
                        .status(AttendanceStatus.EARLY_LEAVE)
                        .tardyReason(before.getTardyReason())
                        .modifyReason(reason)
                        .closed(Boolean.FALSE)
                        .overtimeHours(before.getOvertimeHours())
                        .nightWorkHours(before.getNightWorkHours())
                        .holidayWorkHours(before.getHolidayWorkHours())
                        .unpaidLeave(before.getUnpaidLeave())
                        .build();
        attendanceMapper.insertOrUpdateAttendance(target);
        return attendanceMapper
                .findByEmployeeIdAndWorkDate(before.getEmployeeId(), before.getWorkDate())
                .orElseThrow();
    }

    private void ensureNotClosed(Attendance attendance) {
        if (Boolean.TRUE.equals(attendance.getClosed())) {
            throw new com.reverse.core.exception.BadRequestException(
                    "월 마감된 근태 데이터에는 승인 결과를 반영할 수 없습니다.");
        }
    }

    private void writeHistory(
            Attendance before,
            Attendance after,
            Long actorEmployeeId,
            String actionType,
            String reason) {
        attendanceHistoryMapper.insertHistory(
                AttendanceHistory.builder()
                        .attendanceId(after != null ? after.getAttendanceId() : null)
                        .employeeId(after != null ? after.getEmployeeId() : before.getEmployeeId())
                        .actorEmployeeId(actorEmployeeId)
                        .actionType(actionType)
                        .reason(reason)
                        .workDate(after != null ? after.getWorkDate() : before.getWorkDate())
                        .beforeCheckInTime(before != null ? before.getCheckInTime() : null)
                        .afterCheckInTime(after != null ? after.getCheckInTime() : null)
                        .beforeCheckOutTime(before != null ? before.getCheckOutTime() : null)
                        .afterCheckOutTime(after != null ? after.getCheckOutTime() : null)
                        .beforeTardyReason(before != null ? before.getTardyReason() : null)
                        .afterTardyReason(after != null ? after.getTardyReason() : null)
                        .beforeStatus(before != null ? before.getStatus() : null)
                        .afterStatus(after != null ? after.getStatus() : null)
                        .beforeClosed(before != null ? before.getClosed() : null)
                        .afterClosed(after != null ? after.getClosed() : null)
                        .build());
    }
}
