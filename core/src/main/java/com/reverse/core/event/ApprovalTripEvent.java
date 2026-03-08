package com.reverse.core.event;

import java.time.LocalDateTime;

public record ApprovalTripEvent(
        Long approvalId,
        LocalDateTime approvedAt,
        String tripType,
        String destination,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String reason) {}
