package com.reverse.attendance.internal.dto.response;

import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamWeeklyScheduleEntryResponse {

    private Long weeklyId;
    private Long employeeId;
    private String employeeName;
    private String departmentName;
    private String positionName;
    private LocalDate planDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String workForm;
    private String scheduleTitle;
    private String memo;
    private String rejectReason;
    private ApprovalStatus approvalStatus;
}
