package com.reverse.performance.internal.dto.request;

import com.reverse.core.exception.BadRequestException;

public record PerformanceResultUpdateRequest(
        Integer progress,
        String resultSummary,
        String resultNote,
        String growthPoint,
        String improvementPoint) {

    public PerformanceResultUpdateRequest {
        if (progress == null) {
            throw new BadRequestException("progress는 필수입니다.");
        }
        if (progress < 0 || progress > 100) {
            throw new BadRequestException("progress는 0 이상 100 이하여야 합니다.");
        }
    }
}
