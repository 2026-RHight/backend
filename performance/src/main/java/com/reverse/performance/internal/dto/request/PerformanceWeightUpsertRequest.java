package com.reverse.performance.internal.dto.request;

import java.util.List;

public record PerformanceWeightUpsertRequest(List<PerformanceWeightItemRequest> weights) {}
