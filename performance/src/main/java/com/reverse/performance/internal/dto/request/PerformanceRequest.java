package com.reverse.performance.internal.dto.request;

import com.reverse.performance.internal.domain.Status;
import com.reverse.performance.internal.domain.WorkItem;

import java.time.LocalDate;

public record PerformanceRequest(
        Long performanceId,
        Long employeeId,
        String title,
        WorkItem workItem,
        LocalDate startDate,
        LocalDate expectedEndDate,
        String workDetail,
        Status status,
        Integer achievementRate,
        Integer difficultyScore,
        String comment,
        String feedback
) {
    public PerformanceRequest withEmployeeId(Long employeeId) {
        return new PerformanceRequest(
                performanceId,
                employeeId,
                title,
                workItem,
                startDate,
                expectedEndDate,
                workDetail,
                status,
                achievementRate,
                difficultyScore,
                comment,
                feedback
        );
    }
}
