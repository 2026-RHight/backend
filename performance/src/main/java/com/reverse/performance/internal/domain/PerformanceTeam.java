package com.reverse.performance.internal.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_id")
    private Long id;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "team_result_summary")
    private String teamResultSummary;

    @Column(name = "special_point")
    private String specialPoint;
}
