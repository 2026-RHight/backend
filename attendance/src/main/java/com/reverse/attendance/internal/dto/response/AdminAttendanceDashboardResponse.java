package com.reverse.attendance.internal.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminAttendanceDashboardResponse {

    private String targetMonth;
    private AttendanceSummaryResponse companySummary;
    private long pendingLeaveCount;
    private long pendingOvertimeCount;
    private long pendingBusinessTripCount;
    private long pendingScheduleCount;
    private List<AdminAttendanceReportResponse> topRiskEmployees;
}
