package com.reverse.core.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ApprovalOvertimeEvent(
        Long approvalId,
        LocalDateTime approvedAt,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        String reason) {}
