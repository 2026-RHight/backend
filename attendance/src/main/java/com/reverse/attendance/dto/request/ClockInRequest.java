package com.reverse.attendance.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClockInRequest {

    private Long employeeId;
    private String tardyReason;

}
