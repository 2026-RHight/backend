package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record LeaveDetailRow(
        LocalDateTime startDate, LocalDateTime endDate, String leaveType, String reason) {}
