package com.reverse.attendance.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 관리자용 dto
@Getter
@NoArgsConstructor
public class LeaveProcessRequest {

    private Long leaveRequestId; // 결재할 휴가 신청 건의 ID
    private boolean approve;     // true : 승인, false : 반려
    private String rejectReason; // 반려일 때만 필수 입력
}