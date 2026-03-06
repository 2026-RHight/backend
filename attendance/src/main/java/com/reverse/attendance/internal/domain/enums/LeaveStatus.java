package com.reverse.attendance.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeaveStatus {
    PENDING("대기"),
    APPROVED("승인"),
    REJECTED("반려"),
    CANCELED("취소");

    private final String description;
}