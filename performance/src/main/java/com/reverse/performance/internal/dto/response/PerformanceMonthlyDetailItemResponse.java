package com.reverse.performance.internal.dto.response;

import java.util.List;

public record PerformanceMonthlyDetailItemResponse(
        Long id,
        String type,
        String title,
        String date,
        Integer progress,
        Integer score,
        String description,
        String achievement,
        List<PerformanceFeedbackResponse> feedbacks) {}
