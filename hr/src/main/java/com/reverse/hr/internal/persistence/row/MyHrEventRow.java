package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.HrEventType;
import java.time.LocalDate;

public record MyHrEventRow(
        Long hrEventId,
        HrEventType eventType,
        String eventTitle,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String reason,
        String beforeChange,
        String afterChange) {}
