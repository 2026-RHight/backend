package com.reverse.performance.internal.dto.response;

import java.util.List;

public record PerformanceMonthlyResponse(
        Integer initialYear,
        Integer initialMonth,
        List<PerformanceMonthlyStatResponse> stats,
        List<String> chartLabels,
        List<Integer> myScores,
        List<Integer> teamScores,
        List<PerformanceMonthlyDetailItemResponse> detailItems) {}
