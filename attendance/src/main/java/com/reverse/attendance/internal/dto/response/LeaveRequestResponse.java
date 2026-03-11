package com.reverse.attendance.internal.dto.response;

import com.reverse.attendance.internal.domain.LeaveRequest;
import com.reverse.attendance.internal.domain.enums.LeaveStatus;
import com.reverse.attendance.internal.domain.enums.LeaveType;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeaveRequestResponse {

    private Long leaveRequestId;
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;
    private LeaveStatus leaveStatus;
    private double usedDays;
    private String reason;
    private String rejectReason;

    public static LeaveRequestResponse from(LeaveRequest request) {
        return LeaveRequestResponse.builder()
                .leaveRequestId(request.getLeaveRequestId())
                .employeeId(request.getEmployeeId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .leaveType(request.getLeaveType())
                .leaveStatus(request.getLeaveStatus())
                .usedDays(request.getUsedDays())
                .reason(request.getReason())
                .rejectReason(request.getRejectReason())
                .build();
    }
}
