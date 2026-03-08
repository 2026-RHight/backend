package com.reverse.hr.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HrEventType {

    PROMOTION("직급 변경"),
    TRANSFER("발령"),
    STATE_CHANGE("재직 상태 변경"),
    POSITION_CHANGE("직책 변경"),
    ORG_CHANGE("조직 변경");

    private final String description;
}