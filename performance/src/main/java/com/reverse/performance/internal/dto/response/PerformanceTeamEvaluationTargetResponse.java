package com.reverse.performance.internal.dto.response;

public record PerformanceTeamEvaluationTargetResponse(
        Long id,
        String name,
        String role,
        String department,
        String status,
        Integer systemScore,
        Double peerReviewScore,
        PerformanceTeamEvaluationAveragesResponse peerCriteriaAverages) {}
