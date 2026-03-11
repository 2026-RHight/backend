package com.reverse.attendance.internal.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendanceMonthlyCloseRequest {

    @Min(1900)
    @Max(2100)
    private int year;

    @Min(1)
    @Max(12)
    private int month;
}
