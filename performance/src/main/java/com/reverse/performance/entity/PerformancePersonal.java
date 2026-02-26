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
public class PerformancePersonal {

    @Id
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
