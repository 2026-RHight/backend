package com.reverse.approval.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocType {
    VACATION("휴가 신청서"),
    OVERTIME("연장근무 신청서"),
    FLEXIBLE("유연근무 신청서"),
    TRIP("외근/출장 신청서"),
    LEAVE("휴직 신청서"),
    RTW("복직 신청서");

    private final String description;
}
