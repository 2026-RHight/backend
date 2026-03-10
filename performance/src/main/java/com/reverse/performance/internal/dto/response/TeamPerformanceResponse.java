package com.reverse.performance.internal.dto.response;

import java.time.LocalDate;

public record TeamPerformanceResponse(
        LocalDate startDate,
        LocalDate endDate,
        Integer achievementRate,
        String workDetail,
        String comment) {}
