package com.reverse.attendance.internal.domain;

import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class Overtime {

    private Long overtimeId;
    private Long employeeId;
    private LocalDate workDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private ApprovalStatus approvalStatus;
    private String rejectReason;

    @Builder
    public Overtime(Long overtimeId, Long employeeId
            , LocalDate workDate, LocalDateTime startTime
            , LocalDateTime endTime, String reason
            , ApprovalStatus approvalStatus, String rejectReason)
    {
        this.overtimeId = overtimeId;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.approvalStatus = approvalStatus;
        this.rejectReason = rejectReason;
    }
}