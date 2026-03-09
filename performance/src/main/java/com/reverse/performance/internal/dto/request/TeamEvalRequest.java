package com.reverse.performance.internal.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;

public record TeamEvalRequest(
        @JsonAlias("evaluator_id") Long evaluatorId,
        @JsonAlias("appraisee_id") Long appraiseeId,
        @JsonAlias("performance_eval") String performanceEval,
        @JsonAlias("work_attitude_eval") String workAttitudeEval,
        @JsonAlias("teamwork_eval") String teamworkEval,
        @JsonAlias("solving_eval") String solvingEval,
        Integer performanceScore,
        String performanceComment,
        Integer attitudeScore,
        String attitudeComment,
        Integer collaborationScore,
        String collaborationComment,
        Integer creativityScore,
        String creativityComment
) {
    public TeamEvalRequest withEvaluatorId(Long evaluatorId) {
        return new TeamEvalRequest(
                evaluatorId,
                appraiseeId,
                performanceEval,
                workAttitudeEval,
                teamworkEval,
                solvingEval,
                performanceScore,
                performanceComment,
                attitudeScore,
                attitudeComment,
                collaborationScore,
                collaborationComment,
                creativityScore,
                creativityComment
        );
    }
}
