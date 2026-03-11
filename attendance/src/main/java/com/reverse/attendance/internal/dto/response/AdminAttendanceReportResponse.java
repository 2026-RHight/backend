package com.reverse.attendance.internal.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminAttendanceReportResponse {

    private Long employeeId;
    private String employeeName;
    private String departmentName;
    private int normalCount;
    private int tardyCount;
    private int earlyLeaveCount;
    private int absentCount;
    private int vacationCount;
    private int closedCount;
    private BigDecimal averageWorkHours;
    private BigDecimal leaveUtilizationRate;
    private BigDecimal maxWeeklyWorkHours;
    private boolean weekly52HourExceeded;
    private boolean weekly52HourWarning;
}
