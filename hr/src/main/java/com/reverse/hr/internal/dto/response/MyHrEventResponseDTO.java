package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.HrEventType;
import java.time.LocalDate;

public record MyHrEventResponseDTO(
        Long hrEventId,
        HrEventType eventType,
        String eventTypeDescription,
        String eventTitle,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String reason,
        String beforeChange,
        String afterChange) {}
