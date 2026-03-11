package com.reverse.performance.internal.dto.response;

public record PerformanceDashboardSummaryResponse(
        boolean success,
        Integer year,
        Integer month,
        Integer personalKpiAchievementRate,
        Integer teamKpiAchievementRate,
        Integer monthlyCoreGoalProgressRate,
        Double scoreChangeRate,
        Integer compositeScore) {}
