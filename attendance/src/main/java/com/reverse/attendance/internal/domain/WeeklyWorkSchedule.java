package com.reverse.attendance.internal.domain;

import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeeklyWorkSchedule {
    private Long weeklyId;
    private Long approvalId;
    private Long employeeId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ApprovalStatus approvalStatus; // 기존에 만든 결재 상태 Enum 재사용!
    private LocalDate planDate;
    private String workForm; // OFFICE, REMOTE 등
    private String scheduleTitle;
    private String memo;
    private String rejectReason;
    private String employeeName;
    private String departmentName;
    private String positionName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public WeeklyWorkSchedule(
            Long weeklyId,
            Long approvalId,
            Long employeeId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            ApprovalStatus approvalStatus,
            LocalDate planDate,
            String workForm,
            String scheduleTitle,
            String memo,
            String rejectReason,
            String employeeName,
            String departmentName,
            String positionName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.weeklyId = weeklyId;
        this.approvalId = approvalId;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.approvalStatus = approvalStatus;
        this.planDate = planDate;
        this.workForm = workForm;
        this.scheduleTitle = scheduleTitle;
        this.memo = memo;
        this.rejectReason = rejectReason;
        this.employeeName = employeeName;
        this.departmentName = departmentName;
        this.positionName = positionName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
