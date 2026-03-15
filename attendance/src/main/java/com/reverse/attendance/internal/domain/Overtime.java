package com.reverse.attendance.internal.domain;

import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Overtime {

    private Long overtimeId;
    private Long approvalId;
    private Long employeeId;
    private LocalDate workDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private ApprovalStatus approvalStatus;
    private String rejectReason;
    private String employeeName;

    @Builder
    public Overtime(
            Long overtimeId,
            Long approvalId,
            Long employeeId,
            LocalDate workDate,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String reason,
            ApprovalStatus approvalStatus,
            String rejectReason,
            String employeeName) {
        this.overtimeId = overtimeId;
        this.approvalId = approvalId;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.approvalStatus = approvalStatus;
        this.rejectReason = rejectReason;
        this.employeeName = employeeName;
    }
}
