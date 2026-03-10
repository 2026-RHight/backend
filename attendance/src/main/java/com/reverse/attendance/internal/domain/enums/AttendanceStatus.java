package com.reverse.attendance.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttendanceStatus {
    NORMAL("정상"),
    TARDY("지각"),
    EARLY_LEAVE("조퇴"),
    ABSENT("결근"),
    VACATION("휴가"),
    HALF_VACATION("반차");

    private final String description;
}
