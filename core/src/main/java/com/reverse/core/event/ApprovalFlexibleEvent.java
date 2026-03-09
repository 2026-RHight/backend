package com.reverse.core.event;

import java.time.LocalDateTime;

public record ApprovalFlexibleEvent(
        Long approvalId,
        LocalDateTime approvedAt,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String reason) {}
