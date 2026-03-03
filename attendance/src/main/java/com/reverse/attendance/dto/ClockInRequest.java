package com.reverse.attendance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClockInRequest {

    private Long employeeId;
    private String tardyReason;

}
