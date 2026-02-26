package com.reverse.performance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyPerformance {

    @Id
    @Column(name = "monthly_score_id")
    private Long id;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "score")
    private Integer score;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;
}
