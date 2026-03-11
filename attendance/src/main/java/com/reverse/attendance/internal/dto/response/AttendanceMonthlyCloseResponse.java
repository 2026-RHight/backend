package com.reverse.attendance.internal.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceMonthlyCloseResponse {

    private String targetMonth;
    private int autoClosedCount;
    private int closedCount;
}
