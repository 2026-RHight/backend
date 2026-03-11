package com.reverse.attendance.internal.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceWeeklySummaryResponse {

    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private int standardWeeklyMinutes;
    private int legalMaximumMinutes;
    private int totalWorkedMinutes;
    private int regularWorkedMinutes;
    private int overtimeWorkedMinutes;
    private int progressPercent;
    private boolean weekly52HourExceeded;
    private boolean weekly52HourWarning;
}
