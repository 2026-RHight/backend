package com.reverse.performance.internal.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Evaluation {

    @Id
    @Column(name = "eval_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "evaluator_id")
    private Long evaluatorId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "evaluation_score")
    private Integer evaluationScore;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
