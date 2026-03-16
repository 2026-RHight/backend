package com.reverse.attendance.internal.persistence.row;

import java.time.LocalDateTime;

public record ApprovalVacationBalanceRow(
        String vacationType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String approvalStatus) {}
