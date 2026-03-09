package com.reverse.attendance.internal.domain;

import com.reverse.attendance.internal.domain.enums.LeaveStatus;
import com.reverse.attendance.internal.domain.enums.LeaveType;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LeaveRequest {
    private Long leaveRequestId;
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;
    private LeaveStatus leaveStatus;
    private double usedDays;
    private String reason;
    private String rejectReason;

    @Builder
    public LeaveRequest(
            Long leaveRequestId,
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            LeaveType leaveType,
            LeaveStatus leaveStatus,
            double usedDays,
            String reason,
            String rejectReason) {
        this.leaveRequestId = leaveRequestId;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = leaveType;
        this.leaveStatus = leaveStatus;
        this.usedDays = usedDays;
        this.reason = reason;
        this.rejectReason = rejectReason;
    }
}
