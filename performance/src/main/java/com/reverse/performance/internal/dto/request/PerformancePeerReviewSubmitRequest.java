package com.reverse.performance.internal.dto.request;

public record PerformancePeerReviewSubmitRequest(
        Long appraiseeId,
        Integer communicationScore,
        Integer solvingScore,
        Integer responsibilityScore,
        Integer teamContributionScore,
        Integer cultureContributionScore,
        String comment
) {
}
