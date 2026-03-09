package com.reverse.performance.internal.dto.request;

import java.time.LocalDate;

public record PerformanceRegistrationRequest(
        String type,
        LocalDate startDate,
        LocalDate endDate,
        Integer weight,
        String title,
        String coreTask,
        String content,
        String value
) {
}
