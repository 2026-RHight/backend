package com.reverse.performance.internal.dto.response;

import com.reverse.performance.internal.domain.Status;
import com.reverse.performance.internal.domain.WorkItem;

import java.time.LocalDate;

public record Dashboard(
        Long employeeId,
        WorkItem workItem,
        LocalDate createdAt,
        LocalDate expectedEndDate,
        Status status,
        Integer achievementRate,
        Integer score,
        String feedback
) {
}
