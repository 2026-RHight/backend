package com.reverse.performance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceTeam {

    @Id
    @Column(name = "performance_id")
    private Long id;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "team_result_summary")
    private String teamResultSummary;

    @Column(name = "special_poing")
    private String specialPoint;
}
