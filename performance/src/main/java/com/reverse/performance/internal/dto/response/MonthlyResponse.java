package com.reverse.performance.internal.dto.response;

import com.reverse.performance.internal.domain.WorkItem;

import java.time.LocalDate;

public record MonthlyResponse(
        Long employeeId,
        Long performanceId,
        LocalDate startDate,
        LocalDate endDate,
        WorkItem workItem,
        Integer achievementRate,
        String title,
        Integer score
) {
}
