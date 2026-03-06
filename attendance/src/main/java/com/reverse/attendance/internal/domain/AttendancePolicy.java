package com.reverse.attendance.internal.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class AttendancePolicy {
    private Long policyId;
    private Long employeeId;
    private LocalTime stdStartTime;
    private LocalTime stdEndTime;
    private LocalTime coreTimeStart;
    private LocalTime coreTimeEnd;
    private LocalTime breakTimeStart;
    private LocalTime breakTimeEnd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public AttendancePolicy(Long policyId
            , Long employeeId, LocalTime stdStartTime
            , LocalTime stdEndTime, LocalTime coreTimeStart
            , LocalTime coreTimeEnd, LocalTime breakTimeStart
            , LocalTime breakTimeEnd, LocalDateTime createdAt
            , LocalDateTime updatedAt)
    {
        this.policyId = policyId;
        this.employeeId = employeeId;
        this.stdStartTime = stdStartTime;
        this.stdEndTime = stdEndTime;
        this.coreTimeStart = coreTimeStart;
        this.coreTimeEnd = coreTimeEnd;
        this.breakTimeStart = breakTimeStart;
        this.breakTimeEnd = breakTimeEnd;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

    }

}