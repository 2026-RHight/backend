package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record VacationDetailRow(
        String vacationType, LocalDateTime startDate, LocalDateTime endDate, String reason) {}
