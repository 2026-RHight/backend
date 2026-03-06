package com.reverse.attendance.internal.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeeklyWorkScheduleProcessRequest {

    private Long weeklyId;
    private boolean approve;

}
