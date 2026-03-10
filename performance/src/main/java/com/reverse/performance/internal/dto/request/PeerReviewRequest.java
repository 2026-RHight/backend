package com.reverse.performance.internal.dto.request;

import java.time.LocalDate;

public record PeerReviewRequest(
        Long peerReviewId,
        Long evalId,
        Long reviewerId,
        Integer communicationScore,
        Integer solvingScore,
        Integer responsibilityScore,
        Integer teamContribution,
        Integer cultureContribution,
        String comment,
        Integer evalYear,
        LocalDate createdAt) {
    public PeerReviewRequest withReviewerId(Long reviewerId) {
        return new PeerReviewRequest(
                peerReviewId,
                evalId,
                reviewerId,
                communicationScore,
                solvingScore,
                responsibilityScore,
                teamContribution,
                cultureContribution,
                comment,
                evalYear,
                createdAt);
    }
}
