package com.reverse.performance.internal.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TeamEvalRequest(
        @JsonAlias("evaluator_id") Long evaluatorId,
        @JsonAlias("appraisee_id") Long appraiseeId,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Integer evaluationYear,
        Integer performanceScore,
        String performanceComment,
        Integer attitudeScore,
        String attitudeComment,
        Integer collaborationScore,
        String collaborationComment,
        Integer creativityScore,
        String creativityComment) {
    public TeamEvalRequest withEvaluatorId(Long evaluatorId) {
        return new TeamEvalRequest(
                evaluatorId,
                appraiseeId,
                evaluationYear,
                performanceScore,
                performanceComment,
                attitudeScore,
                attitudeComment,
                collaborationScore,
                collaborationComment,
                creativityScore,
                creativityComment);
    }

    public TeamEvalRequest withEvaluationYear(Integer evaluationYear) {
        return new TeamEvalRequest(
                evaluatorId,
                appraiseeId,
                evaluationYear,
                performanceScore,
                performanceComment,
                attitudeScore,
                attitudeComment,
                collaborationScore,
                collaborationComment,
                creativityScore,
                creativityComment);
    }
}
