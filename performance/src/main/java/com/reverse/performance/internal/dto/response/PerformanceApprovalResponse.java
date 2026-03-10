package com.reverse.performance.internal.dto.response;

import java.util.List;

public record PerformanceApprovalResponse(
        List<PerformanceApprovalItemResponse> planItems,
        List<PerformanceApprovalItemResponse> resultItems) {}
