package com.reverse.core.event;

import java.time.LocalDateTime;

public record ApprovalLeaveEvent(
        Long approvalId,
        LocalDateTime approvedAt,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String leaveType,
        String reason) {}
