package com.reverse.hr.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrgType {
    COMPANY("회사"),
    HEADQUARTER("본부"),
    CENTER("센터"),
    DIVISION("부문"),
    DEPARTMENT("부"),
    TEAM("팀");

    private final String description;
}
