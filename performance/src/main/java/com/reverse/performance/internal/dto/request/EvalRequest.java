package com.reverse.performance.internal.dto.request;

import java.time.LocalDateTime;

public record EvalRequest(
        Long evalId,
        Long employeeId,
        Long approvalId,
        Integer year,
        Integer evaluationScore,
        LocalDateTime confirmAt,
        LocalDateTime createdAt
) {
    public EvalRequest withApprovalId(Long approvalId) {
        return new EvalRequest(
                evalId,
                employeeId,
                approvalId,
                year,
                evaluationScore,
                confirmAt,
                createdAt
        );
    }
}
