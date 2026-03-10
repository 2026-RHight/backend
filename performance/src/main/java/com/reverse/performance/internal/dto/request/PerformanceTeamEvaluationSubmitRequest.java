package com.reverse.performance.internal.dto.request;

public record PerformanceTeamEvaluationSubmitRequest(
        Long appraiseeId,
        Integer performanceScore,
        String performanceComment,
        Integer attitudeScore,
        String attitudeComment,
        Integer collaborationScore,
        String collaborationComment,
        Integer creativityScore,
        String creativityComment) {}
