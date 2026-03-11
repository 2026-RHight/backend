package com.reverse.attendance.internal.domain;

import com.reverse.attendance.internal.domain.enums.AttendanceStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendanceHistory {

    private Long historyId;
    private Long attendanceId;
    private Long employeeId;
    private Long actorEmployeeId;
    private String actionType;
    private String reason;
    private LocalDate workDate;
    private LocalTime beforeCheckInTime;
    private LocalTime afterCheckInTime;
    private LocalTime beforeCheckOutTime;
    private LocalTime afterCheckOutTime;
    private AttendanceStatus beforeStatus;
    private AttendanceStatus afterStatus;
    private Boolean beforeClosed;
    private Boolean afterClosed;
    private LocalDateTime createdAt;

    @Builder
    public AttendanceHistory(
            Long historyId,
            Long attendanceId,
            Long employeeId,
            Long actorEmployeeId,
            String actionType,
            String reason,
            LocalDate workDate,
            LocalTime beforeCheckInTime,
            LocalTime afterCheckInTime,
            LocalTime beforeCheckOutTime,
            LocalTime afterCheckOutTime,
            AttendanceStatus beforeStatus,
            AttendanceStatus afterStatus,
            Boolean beforeClosed,
            Boolean afterClosed,
            LocalDateTime createdAt) {
        this.historyId = historyId;
        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.actorEmployeeId = actorEmployeeId;
        this.actionType = actionType;
        this.reason = reason;
        this.workDate = workDate;
        this.beforeCheckInTime = beforeCheckInTime;
        this.afterCheckInTime = afterCheckInTime;
        this.beforeCheckOutTime = beforeCheckOutTime;
        this.afterCheckOutTime = afterCheckOutTime;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.beforeClosed = beforeClosed;
        this.afterClosed = afterClosed;
        this.createdAt = createdAt;
    }
}
