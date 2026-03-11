package com.reverse.performance.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_evaluation_id")
    private Long id;

    @Column(name = "evaluator_id")
    private Long evaluatorId;

    @Column(name = "appraisee_id")
    private Long appraiseeId;

    @Column(name = "evaluation_year")
    private Integer evaluationYear;

    @Column(name = "performance_score")
    private Integer performanceScore;

    @Column(name = "performance_comment")
    private String performanceComment;

    @Column(name = "attitude_score")
    private Integer attitudeScore;

    @Column(name = "attitude_comment")
    private String attitudeComment;

    @Column(name = "collaboration_score")
    private Integer collaborationScore;

    @Column(name = "collaboration_comment")
    private String collaborationComment;

    @Column(name = "creativity_score")
    private Integer creativityScore;

    @Column(name = "creativity_comment")
    private String creativityComment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
