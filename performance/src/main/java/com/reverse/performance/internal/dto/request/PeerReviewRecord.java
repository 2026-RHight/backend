package com.reverse.performance.internal.dto.request;

import java.time.LocalDate;

public record PeerReviewRecord(
        Long peerReviewId,
        Long evalId,
        Long reviewerId,
        Integer communicationScore,
        Integer solvingScore,
        Integer responsibilityScore,
        Integer teamContribution,
        String comment,
        Integer evalYear,
        LocalDate createdAt
) {
}
