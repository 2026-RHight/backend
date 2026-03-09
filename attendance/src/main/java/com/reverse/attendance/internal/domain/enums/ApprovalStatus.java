package com.reverse.attendance.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApprovalStatus {
    /* LeaveStatus와 겹치지만, 도메인 간 결합도를 낮추거나 나중에 연장근무에서도 공통으로 쓰기 위해 새로 만듦. */
    PENDING("대기"),
    APPROVED("승인"),
    REJECTED("반려"),
    CANCELED("취소");

    private final String description;
}
