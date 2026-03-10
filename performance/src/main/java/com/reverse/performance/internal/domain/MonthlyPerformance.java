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
public class MonthlyPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_performance_id")
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
