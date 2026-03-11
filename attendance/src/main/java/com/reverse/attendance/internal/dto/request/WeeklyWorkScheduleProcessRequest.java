package com.reverse.attendance.internal.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeeklyWorkScheduleProcessRequest {

    private Long weeklyId;
    private boolean approve;

    @Size(max = 255)
    private String rejectReason;
}
