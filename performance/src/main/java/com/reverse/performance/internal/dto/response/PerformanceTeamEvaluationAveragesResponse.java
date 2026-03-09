package com.reverse.performance.internal.dto.response;

public record PerformanceTeamEvaluationAveragesResponse(
        Double performance,
        Double attitude,
        Double collaboration,
        Double creativity) {}
