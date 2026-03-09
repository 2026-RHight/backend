package com.reverse.attendance.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OvertimeApplyRequest {

    @NotNull private LocalDate workDate;
    @NotNull private LocalDateTime startTime;
    @NotNull private LocalDateTime endTime;
    @NotBlank private String reason;
}
