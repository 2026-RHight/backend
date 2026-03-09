package com.reverse.attendance.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TripType {
    OUTSIDE_WORK("외근"),
    BUSINESS_TRIP("출장");

    private final String description;
}
