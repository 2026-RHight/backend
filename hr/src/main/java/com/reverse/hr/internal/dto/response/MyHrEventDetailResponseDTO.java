package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.HrEventStatus;
import com.reverse.hr.internal.domain.enums.HrEventType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MyHrEventDetailResponseDTO(
        Long hrEventId,
        HrEventType eventType,
        String eventTypeDescription,
        String eventTitle,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String reason,
        String beforeChange,
        String afterChange,
        HrEventStatus eventStatus,
        String eventStatusDescription,
        LocalDateTime appliedAt) {}
