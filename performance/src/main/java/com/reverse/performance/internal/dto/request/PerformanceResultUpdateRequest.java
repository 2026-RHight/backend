package com.reverse.performance.internal.dto.request;

public record PerformanceResultUpdateRequest(
        Integer progress,
        String resultSummary,
        String resultNote,
        String growthPoint,
        String improvementPoint) {}
