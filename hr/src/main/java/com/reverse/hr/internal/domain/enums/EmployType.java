package com.reverse.hr.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmployType {

    REGULAR("정규직"),
    NON_REGULAR("비정규직"),
    CONTRACT("계약직");

    private final String description;
}
