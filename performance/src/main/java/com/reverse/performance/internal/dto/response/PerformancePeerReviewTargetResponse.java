package com.reverse.performance.internal.dto.response;

public record PerformancePeerReviewTargetResponse(
        Long id,
        String name,
        String team,
        boolean evaluated) {}
