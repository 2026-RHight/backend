package com.reverse.performance.internal.dto.request;

public record PerformancePersonalRequest(
        Long performanceId,
        String expectedValue,
        String resultSummary,
        String growthPoint,
        String improvement
) {
}
