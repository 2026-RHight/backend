package com.reverse.attendance.internal.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceSummaryResponse {

    private int normalCount;
    private int tardyCount;
    private int earlyLeaveCount;
    private int absentCount;
    private int vacationCount;

}
