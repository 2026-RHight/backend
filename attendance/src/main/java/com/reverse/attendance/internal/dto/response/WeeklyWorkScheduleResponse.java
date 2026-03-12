package com.reverse.attendance.internal.dto.response;

import com.reverse.attendance.internal.domain.WeeklyWorkSchedule;
import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeeklyWorkScheduleResponse {

    private Long weeklyId;
    private Long employeeId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ApprovalStatus approvalStatus;
    private LocalDate planDate;
    private String workForm;
    private String scheduleTitle;
    private String memo;
    private String rejectReason;
    private String employeeName;
    private String departmentName;
    private String positionName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WeeklyWorkScheduleResponse from(WeeklyWorkSchedule schedule) {
        return WeeklyWorkScheduleResponse.builder()
                .weeklyId(schedule.getWeeklyId())
                .employeeId(schedule.getEmployeeId())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .approvalStatus(schedule.getApprovalStatus())
                .planDate(schedule.getPlanDate())
                .workForm(schedule.getWorkForm())
                .scheduleTitle(schedule.getScheduleTitle())
                .memo(schedule.getMemo())
                .rejectReason(schedule.getRejectReason())
                .employeeName(schedule.getEmployeeName())
                .departmentName(schedule.getDepartmentName())
                .positionName(schedule.getPositionName())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}
