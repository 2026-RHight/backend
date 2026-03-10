package com.reverse.performance.internal.dto.response;

import com.reverse.performance.internal.domain.Status;
import com.reverse.performance.internal.domain.WorkItem;
import java.time.LocalDate;

public record EvaluatorPerformanceResponse(
        Long performanceId,
        Long employeeId,
        WorkItem workItem,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        Status status,
        Integer achievementRate,
        String comment) {}
