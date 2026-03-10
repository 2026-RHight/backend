package com.reverse.performance.internal.dto.response;

import com.reverse.performance.internal.domain.WorkItem;
import java.time.LocalDate;

public record AppraiseePerformance(
        Long evaluatorId,
        Long employeeId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate createdAt,
        WorkItem workItem,
        Integer achievementRate,
        String workDetail) {}
