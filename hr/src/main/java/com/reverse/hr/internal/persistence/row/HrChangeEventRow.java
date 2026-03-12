package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.HrEventStatus;
import com.reverse.hr.internal.domain.enums.HrEventType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record HrChangeEventRow(
        Long hrEventId,
        Long employeeId,
        String employeeName,
        HrEventType eventType,
        String eventTitle,
        String beforeChange,
        String afterChange,
        String reason,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        HrEventStatus eventStatus,
        LocalDateTime appliedAt,
        String appliedError) {}
