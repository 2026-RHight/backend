package com.reverse.attendance.internal.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceCalendarResponse {

    private String targetMonth;
    private int year;
    private int month;
    private List<AttendanceCalendarEventResponse> events;
}
