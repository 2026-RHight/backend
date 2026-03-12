package com.reverse.hr.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HrEventStatus {
    PENDING("대기"),
    APPLIED("반영 완료"),
    FAILED("반영 실패");

    private final String description;
}
