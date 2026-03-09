package com.reverse.performance.internal.dto.response;

import java.util.List;

public record PerformanceDashboardResponse(
        Integer pendingApprovalCount,
        List<String> trendLabels,
        List<Integer> trendScores,
        List<PerformanceFeedbackResponse> feedbacks) {}
