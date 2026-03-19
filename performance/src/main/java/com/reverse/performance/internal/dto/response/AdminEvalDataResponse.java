package com.reverse.performance.internal.dto.response;

public record AdminEvalDataResponse(
        Integer teamEvalScore,
        Double peerReviewScore,
        Integer systemScore,
        Integer finalScore,
        String finalGrade) {}
