package com.reverse.core.event;

import java.time.LocalDateTime;

public record ApprovalVacationEvent(
        Long approvalId,
        LocalDateTime approvedAt,
        String vacationType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String reason) {}
