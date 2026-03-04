package com.reverse.approval.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VacationType {
    ANNUAL("연가"),
    HALF("반차"),
    SICK("병가"),
    ETC("기타");

    private final String description;

}
