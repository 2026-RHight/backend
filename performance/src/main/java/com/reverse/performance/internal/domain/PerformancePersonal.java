package com.reverse.performance.internal.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PerformancePersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_id")
    private Long id;

    @Column(name = "expected_value")
    private String expectedValue;

    @Column(name = "result_summary")
    private String resultSummary;

    @Column(name = "growth_point")
    private String growthPoint;

    @Column(name = "improvement")
    private String improvement;
}
