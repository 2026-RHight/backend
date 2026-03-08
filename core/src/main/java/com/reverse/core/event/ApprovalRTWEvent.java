package com.reverse.core.event;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApprovalRTWEvent(
        Long approvalId, LocalDateTime approvedAt, LocalDate rtwDate, String reason) {}
