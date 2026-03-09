package com.reverse.performance.internal.dto.response;

public record PerformanceFeedbackResponse(
        Long id,
        String text,
        String author,
        String date,
        Long inquiryItemId,
        String detail) {}
