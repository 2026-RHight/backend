package com.reverse.hr.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmployeeState {

    WORK("재직"),
    LEAVE("휴직"),
    RESIGN("사직");

    private final String description;
}
