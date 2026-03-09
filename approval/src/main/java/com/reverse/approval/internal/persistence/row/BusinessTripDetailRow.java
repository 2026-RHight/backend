package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record BusinessTripDetailRow(
        String tripType,
        String destination,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String reason) {}
