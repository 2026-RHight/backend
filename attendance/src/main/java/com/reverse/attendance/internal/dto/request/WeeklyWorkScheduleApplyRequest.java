package com.reverse.attendance.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyWorkScheduleApplyRequest {

    @NotNull private LocalDateTime startDate;
    @NotNull private LocalDateTime endDate;
    @NotNull private LocalDate planDate;
    @NotBlank private String workForm;
    @NotBlank private String scheduleTitle;
    private String memo;
}
