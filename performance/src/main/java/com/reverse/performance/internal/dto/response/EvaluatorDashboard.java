package com.reverse.performance.internal.dto.response;

import com.reverse.performance.internal.domain.WorkItem;

import java.time.LocalDate;

public record EvaluatorDashboard(
        Long performanceId,
        Long employeeId,
        String title,
        WorkItem workItem,
        LocalDate createdAt,
        Long attachmentId,
        String fileName,
        String fileUrl
) {
}
