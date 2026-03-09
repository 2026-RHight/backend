package com.reverse.attendance.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeaveType {
    ANNUAL("연차", 1.0),
    HALF_AM("오전반차", 0.5),
    HALF_PM("오후반차", 0.5),
    SPECIAL("특별휴가", 0.0); // 차감 없는 휴가

    private final String description;
    private final double deductionDays; // 차감 일수
}
