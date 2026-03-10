package com.reverse.performance.internal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PerformancePersonalRequest(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long performanceId,
        String expectedValue,
        String resultSummary,
        String growthPoint,
        String improvement) {
    public PerformancePersonalRequest withPerformanceId(Long performanceId) {
        return new PerformancePersonalRequest(
                performanceId, expectedValue, resultSummary, growthPoint, improvement);
    }
}
