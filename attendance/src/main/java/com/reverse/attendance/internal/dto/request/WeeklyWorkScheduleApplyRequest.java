package com.reverse.attendance.internal.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class WeeklyWorkScheduleApplyRequest {

    private Long employeeId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDate planDate;
    private String workForm;
    private String scheduleTitle;
    private String memo;

}
