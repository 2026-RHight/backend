package com.reverse.attendance.internal.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamWeeklyScheduleOverviewResponse {

    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private List<TeamWeeklyScheduleDayResponse> days;
}
