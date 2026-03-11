package com.reverse.attendance.internal.dto.response;

import com.reverse.attendance.internal.domain.AttendanceHistory;
import com.reverse.attendance.internal.domain.enums.AttendanceStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceHistoryResponse {

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

    public static AttendanceHistoryResponse from(AttendanceHistory history) {
        return AttendanceHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .attendanceId(history.getAttendanceId())
                .employeeId(history.getEmployeeId())
                .actorEmployeeId(history.getActorEmployeeId())
                .actionType(history.getActionType())
                .reason(history.getReason())
                .workDate(history.getWorkDate())
                .beforeCheckInTime(history.getBeforeCheckInTime())
                .afterCheckInTime(history.getAfterCheckInTime())
                .beforeCheckOutTime(history.getBeforeCheckOutTime())
                .afterCheckOutTime(history.getAfterCheckOutTime())
                .beforeStatus(history.getBeforeStatus())
                .afterStatus(history.getAfterStatus())
                .beforeClosed(history.getBeforeClosed())
                .afterClosed(history.getAfterClosed())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
