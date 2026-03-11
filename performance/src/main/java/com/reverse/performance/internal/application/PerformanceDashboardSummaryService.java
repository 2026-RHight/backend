package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.response.PerformanceDashboardSummaryResponse;
import com.reverse.performance.internal.persistence.PerformanceDashboardSummaryMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceDashboardSummaryService {

    private final PerformanceHrMemberResolver performanceHrMemberResolver;
    private final PerformanceDashboardSummaryMapper performanceDashboardSummaryMapper;

    @Transactional
    public PerformanceDashboardSummaryResponse recalculateCurrentMonth(Long employeeId) {
        YearMonth currentMonth = YearMonth.now();
        return recalculate(employeeId, currentMonth.getYear(), currentMonth.getMonthValue());
    }

    @Transactional
    public PerformanceDashboardSummaryResponse recalculate(
            Long employeeId, Integer targetYear, Integer targetMonth) {
        try {
            validateTargetMonth(targetYear, targetMonth);
            Long orgId = performanceHrMemberResolver.resolveOrgId(employeeId);
            int personalKpiAchievementRate =
                    nvl(
                            performanceDashboardSummaryMapper.findPersonalKpiAchievementRate(
                                    employeeId, targetYear, targetMonth));
            int teamKpiAchievementRate =
                    nvl(
                            performanceDashboardSummaryMapper.findTeamKpiAchievementRate(
                                    employeeId, targetYear, targetMonth));
            int monthlyCoreGoalProgressRate =
                    nvl(
                            performanceDashboardSummaryMapper.findMonthlyCoreGoalProgressRate(
                                    employeeId, targetYear, targetMonth));
            int compositeScore =
                    nvl(
                            performanceDashboardSummaryMapper.findCompositeScore(
                                    orgId, employeeId, targetYear, targetMonth));

            PerformanceDashboardSummaryMapper.DashboardSummaryRow previousSummary =
                    performanceDashboardSummaryMapper.findDashboardSummary(
                            employeeId,
                            YearMonth.of(targetYear, targetMonth).minusMonths(1).getYear(),
                            YearMonth.of(targetYear, targetMonth).minusMonths(1).getMonthValue());
            int previousCompositeScore =
                    previousSummary == null ? 0 : nvl(previousSummary.compositeScore());
            BigDecimal scoreChangeRate =
                    calculateScoreChangeRate(compositeScore, previousCompositeScore);

            performanceDashboardSummaryMapper.upsertDashboardSummary(
                    employeeId,
                    targetYear,
                    targetMonth,
                    personalKpiAchievementRate,
                    teamKpiAchievementRate,
                    monthlyCoreGoalProgressRate,
                    scoreChangeRate,
                    compositeScore);

            return toResponse(
                    performanceDashboardSummaryMapper.findDashboardSummary(
                            employeeId, targetYear, targetMonth));
        } catch (BadSqlGrammarException | IllegalStateException | DateTimeException ex) {
            log.warn(
                    "성과 대시보드 요약 재계산을 건너뜁니다. 스키마 또는 HR facade 구성이 완전하지 않을 수 있습니다. employeeId={}, year={}, month={}",
                    employeeId,
                    targetYear,
                    targetMonth,
                    ex);
            return new PerformanceDashboardSummaryResponse(
                    targetYear, targetMonth, 0, 0, 0, 0.0, 0);
        }
    }

    @Transactional(readOnly = true)
    public PerformanceDashboardSummaryResponse getCurrentMonthSummary(Long employeeId) {
        YearMonth currentMonth = YearMonth.now();
        try {
            PerformanceDashboardSummaryMapper.DashboardSummaryRow currentSummary =
                    performanceDashboardSummaryMapper.findDashboardSummary(
                            employeeId, currentMonth.getYear(), currentMonth.getMonthValue());
            if (currentSummary != null) {
                return toResponse(currentSummary);
            }
        } catch (BadSqlGrammarException | IllegalStateException ex) {
            log.warn(
                    "성과 대시보드 요약 조회를 건너뜁니다. 스키마 또는 HR facade 구성이 완전하지 않을 수 있습니다. employeeId={}",
                    employeeId,
                    ex);
        }
        return new PerformanceDashboardSummaryResponse(
                currentMonth.getYear(), currentMonth.getMonthValue(), 0, 0, 0, 0.0, 0);
    }

    private void validateTargetMonth(Integer targetYear, Integer targetMonth) {
        if (targetYear == null || targetMonth == null) {
            throw new IllegalArgumentException("targetYear and targetMonth are required");
        }
        YearMonth.of(targetYear, targetMonth);
    }

    private PerformanceDashboardSummaryResponse toResponse(
            PerformanceDashboardSummaryMapper.DashboardSummaryRow row) {
        if (row == null) {
            return new PerformanceDashboardSummaryResponse(null, null, 0, 0, 0, 0.0, 0);
        }
        return new PerformanceDashboardSummaryResponse(
                row.metricYear(),
                row.metricMonth(),
                nvl(row.personalKpiAchievementRate()),
                nvl(row.teamKpiAchievementRate()),
                nvl(row.monthlyCoreGoalProgressRate()),
                decimalOrZero(row.scoreChangeRate()).doubleValue(),
                nvl(row.compositeScore()));
    }

    private BigDecimal calculateScoreChangeRate(int currentScore, int previousScore) {
        if (previousScore <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(currentScore - previousScore)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previousScore), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal decimalOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value;
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }
}
