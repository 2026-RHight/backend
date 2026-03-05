package com.reverse.approval.internal.persistence.param;

import com.reverse.approval.internal.domain.enums.LeaveType;
import com.reverse.approval.internal.dto.request.LeaveRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LeaveDetailParam {
    private final Long approvalId;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final LeaveType leaveType;
    private final String reason;

    @Builder
    public LeaveDetailParam(Long approvalId, LocalDateTime startDate, LocalDateTime endDate, LeaveType leaveType, String reason) {
        this.approvalId = approvalId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = leaveType;
        this.reason = reason;
    }

    public static LeaveDetailParam from(LeaveRequest dto, Long approvalId) {
        return LeaveDetailParam.builder()
                .approvalId(approvalId)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .leaveType(dto.getLeaveType())
                .reason(dto.getReason())
                .build();
    }
}
