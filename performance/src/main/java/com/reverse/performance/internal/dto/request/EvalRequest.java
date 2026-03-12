package com.reverse.performance.internal.dto.request;

import java.time.LocalDateTime;

public record EvalRequest(
        Long evalId,
        Long employeeId,
        Long evaluatorId,
        Integer year,
        Integer evaluationScore,
        LocalDateTime createdAt) {
    public EvalRequest withEvaluatorId(Long evaluatorId) {
        return new EvalRequest(evalId, employeeId, evaluatorId, year, evaluationScore, createdAt);
    }
}
