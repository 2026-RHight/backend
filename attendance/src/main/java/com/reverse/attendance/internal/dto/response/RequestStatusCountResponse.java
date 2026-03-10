package com.reverse.attendance.internal.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestStatusCountResponse {
    private int pendingCount;
    private int approvedCount;
    private int rejectedCount;
}
