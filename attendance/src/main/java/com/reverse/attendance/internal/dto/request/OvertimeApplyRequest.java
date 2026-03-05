package com.reverse.attendance.internal.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class OvertimeApplyRequest {

    private LocalDate workDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;

}