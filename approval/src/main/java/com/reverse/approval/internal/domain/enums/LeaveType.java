package com.reverse.approval.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeaveType {
    PARENTAL_LEAVE("육아 휴직"),
    SICK_LEAVE("질병 휴직"),
    FAMILY_CARE_LEAVE("가족돌봄휴직");

    private final String description;
}
