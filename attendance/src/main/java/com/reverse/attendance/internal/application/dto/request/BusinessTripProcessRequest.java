package com.reverse.attendance.internal.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BusinessTripProcessRequest {       /* 관리자 결재용 */

    private Long tripId;
    private boolean approve;        // true면 승인, false면 반려
    private String rejectReason;

}
