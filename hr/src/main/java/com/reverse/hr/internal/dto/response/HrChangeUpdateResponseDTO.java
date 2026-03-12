package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.HrEventType;

public record HrChangeUpdateResponseDTO(
        Long employeeId, Long hrEventId, HrEventType eventType, String eventTypeDescription) {}
