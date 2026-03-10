package com.reverse.performance.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "performance_personal")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PerformancePersonal {

    @Id
    // performance_id is assigned from the parent performance row.
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
