package com.reverse.performance.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PeerReview {

    @Id
    @Column(name = "peer_review_id")
    private Long id;

    @Column(name = "eval_id")
    private Long evalId;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "communication_score")
    private Integer communicationScore;

    @Column(name = "solving_score")
    private Integer solvingScore;

    @Column(name = "responsibility_score")
    private Integer responsibilityScore;

    @Column(name = "team_contribution")
    private Integer teamContribution;

    @Column(name = "culture_contribution")
    private Integer cultureContribution;

    @Column(name = "comment")
    private String comment;

    @Column(name = "eval_year")
    private Integer evalYear;

    @Column(name = "created_at")
    private LocalDate createdAt;
}
