package com.reverse.attendance.internal.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendancePolicyUpsertRequest {

    @NotNull private LocalTime stdStartTime;
    @NotNull private LocalTime stdEndTime;
    @NotNull private LocalTime coreTimeStart;
    @NotNull private LocalTime coreTimeEnd;
    @NotNull private LocalTime breakTimeStart;
    @NotNull private LocalTime breakTimeEnd;
}
