package com.reverse.attendance.internal.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamWeeklyScheduleDayResponse {

    private LocalDate planDate;
    private int requestedEmployeeCount;
    private int approvedEmployeeCount;
    private boolean coreTimeShortageRisk;
    private List<TeamWeeklyScheduleEntryResponse> entries;
}
