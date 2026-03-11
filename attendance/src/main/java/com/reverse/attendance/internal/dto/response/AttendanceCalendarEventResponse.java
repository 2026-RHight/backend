package com.reverse.attendance.internal.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceCalendarEventResponse {

    private String eventId;
    private String category;
    private String title;
    private String status;
    private LocalDate targetDate;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String memo;
}
