package com.reverse.performance.internal.dto.response;

import java.time.LocalDateTime;

public record PerformanceWeightResponse(
        Long orgId, Integer personalWeightRate, Integer teamWeightRate, LocalDateTime updatedAt) {}
