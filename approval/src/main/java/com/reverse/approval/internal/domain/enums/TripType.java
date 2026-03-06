package com.reverse.approval.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TripType {
    OUTSIDE("외근"),
    BUSINESSTRIP("출장");

    private final String description;
}