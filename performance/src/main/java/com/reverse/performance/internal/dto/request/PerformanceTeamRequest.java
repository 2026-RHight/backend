package com.reverse.performance.internal.dto.request;

public record PerformanceTeamRequest(
        Long performanceId,
        Integer weight,
        String teamResultSummary,
        String specialPoint
) {
}
