package com.reverse.hr.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitType {
    NEW("신입"),
    EXPERIENCED("경력");

    private final String description;
}
