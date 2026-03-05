package com.reverse.attendance.internal.dto.request;

import com.reverse.attendance.internal.domain.enums.LeaveType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class LeaveApplyRequest {

    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;
    private String reason;

}