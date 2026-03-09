package com.reverse.attendance.internal.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClockInRequest {

    private String tardyReason;
}
