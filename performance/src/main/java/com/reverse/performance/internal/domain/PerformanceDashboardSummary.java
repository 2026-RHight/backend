package com.reverse.performance.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "performance_metric_summary")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceDashboardSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_metric_summary_id")
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "metric_year")
    private Integer metricYear;

    @Column(name = "metric_month")
    private Integer metricMonth;

    @Column(name = "personal_kpi_achievement_rate")
    private Integer personalKpiAchievementRate;

    @Column(name = "team_kpi_achievement_rate")
    private Integer teamKpiAchievementRate;

    @Column(name = "monthly_core_goal_progress_rate")
    private Integer monthlyCoreGoalProgressRate;

    @Column(name = "score_change_rate")
    private BigDecimal scoreChangeRate;

    @Column(name = "composite_score")
    private Integer compositeScore;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;
}
