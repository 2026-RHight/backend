package com.reverse.attendance.internal.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OvertimeProcessRequest {

    private Long overtimeId;
    private boolean approve;
    private String rejectReason;
}
