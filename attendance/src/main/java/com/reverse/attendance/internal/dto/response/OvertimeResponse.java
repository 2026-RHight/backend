package com.reverse.attendance.internal.dto.response;

import com.reverse.attendance.internal.domain.Overtime;
import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OvertimeResponse {

    private Long overtimeId;
    private Long employeeId;
    private LocalDate workDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private ApprovalStatus approvalStatus;
    private String rejectReason;

    public static OvertimeResponse from(Overtime overtime) {
        return OvertimeResponse.builder()
                .overtimeId(overtime.getOvertimeId())
                .employeeId(overtime.getEmployeeId())
                .workDate(overtime.getWorkDate())
                .startTime(overtime.getStartTime())
                .endTime(overtime.getEndTime())
                .reason(overtime.getReason())
                .approvalStatus(overtime.getApprovalStatus())
                .rejectReason(overtime.getRejectReason())
                .build();
    }
}
