package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.HrEventStatus;
import com.reverse.hr.internal.domain.enums.HrEventType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record HrEventItemRow(
        Long hrEventId,
        HrEventType eventType,
        String eventTitle,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String reason,
        String beforeChange,
        String afterChange,
        Long sourceApprovalId,
        HrEventStatus eventStatus,
        LocalDateTime appliedAt,
        String appliedError) {}
