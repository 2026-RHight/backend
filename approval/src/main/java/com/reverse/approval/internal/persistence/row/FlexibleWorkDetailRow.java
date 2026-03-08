package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record FlexibleWorkDetailRow(
        LocalDateTime startDate, LocalDateTime endDate, String reason) {}
