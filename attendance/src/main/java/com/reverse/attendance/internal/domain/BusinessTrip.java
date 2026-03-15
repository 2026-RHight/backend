package com.reverse.attendance.internal.domain;

import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import com.reverse.attendance.internal.domain.enums.TripType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BusinessTrip {

    private Long tripId;
    private Long approvalId;
    private Long employeeId;
    private TripType tripType;
    private String destination;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String reason;
    private ApprovalStatus approvalStatus;
    private String rejectReason;
    private String employeeName;

    @Builder
    public BusinessTrip(
            Long tripId,
            Long approvalId,
            Long employeeId,
            TripType tripType,
            String destination,
            LocalDateTime startDatetime,
            LocalDateTime endDatetime,
            String reason,
            ApprovalStatus approvalStatus,
            String rejectReason,
            String employeeName) {
        this.tripId = tripId;
        this.approvalId = approvalId;
        this.employeeId = employeeId;
        this.tripType = tripType;
        this.destination = destination;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
        this.reason = reason;
        this.approvalStatus = approvalStatus;
        this.rejectReason = rejectReason;
        this.employeeName = employeeName;
    }
}
