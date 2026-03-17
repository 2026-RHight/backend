package com.reverse.performance.internal.dto.request;

public record PerformanceWeightItemRequest(
        Long orgId, Integer personalWeightRate, Integer teamWeightRate) {}
