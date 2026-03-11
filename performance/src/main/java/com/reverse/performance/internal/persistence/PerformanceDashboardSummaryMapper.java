package com.reverse.performance.internal.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PerformanceDashboardSummaryMapper {
    Integer findPersonalKpiAchievementRate(
            @Param("employeeId") Long employeeId,
            @Param("targetYear") Integer targetYear,
            @Param("targetMonth") Integer targetMonth);

    Integer findTeamKpiAchievementRate(
            @Param("employeeId") Long employeeId,
            @Param("targetYear") Integer targetYear,
            @Param("targetMonth") Integer targetMonth);

    Integer findMonthlyCoreGoalProgressRate(
            @Param("employeeId") Long employeeId,
            @Param("targetYear") Integer targetYear,
            @Param("targetMonth") Integer targetMonth);

    Integer findCompositeScore(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("targetYear") Integer targetYear,
            @Param("targetMonth") Integer targetMonth);

    void upsertDashboardSummary(
            @Param("employeeId") Long employeeId,
            @Param("targetYear") Integer targetYear,
            @Param("targetMonth") Integer targetMonth,
            @Param("personalKpiAchievementRate") Integer personalKpiAchievementRate,
            @Param("teamKpiAchievementRate") Integer teamKpiAchievementRate,
            @Param("monthlyCoreGoalProgressRate") Integer monthlyCoreGoalProgressRate,
            @Param("scoreChangeRate") BigDecimal scoreChangeRate,
            @Param("compositeScore") Integer compositeScore);

    DashboardSummaryRow findDashboardSummary(
            @Param("employeeId") Long employeeId,
            @Param("targetYear") Integer targetYear,
            @Param("targetMonth") Integer targetMonth);

    DashboardSummaryRow findLatestDashboardSummary(@Param("employeeId") Long employeeId);

    record DashboardSummaryRow(
            Long employeeId,
            Integer metricYear,
            Integer metricMonth,
            Integer personalKpiAchievementRate,
            Integer teamKpiAchievementRate,
            Integer monthlyCoreGoalProgressRate,
            BigDecimal scoreChangeRate,
            Integer compositeScore,
            LocalDateTime calculatedAt) {}
}
