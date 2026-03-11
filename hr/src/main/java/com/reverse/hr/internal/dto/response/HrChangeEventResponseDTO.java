package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.HrEventStatus;
import com.reverse.hr.internal.domain.enums.HrEventType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record HrChangeEventResponseDTO(
        Long hrEventId,
        Long employeeId,
        String employeeName,
        HrEventType eventType,
        String eventTypeDescription,
        String eventTitle,
        String beforeChange,
        String afterChange,
        String reason,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String handlerName,
        HrEventStatus eventStatus,
        String eventStatusDescription,
        LocalDateTime appliedAt,
        String appliedError) {}
