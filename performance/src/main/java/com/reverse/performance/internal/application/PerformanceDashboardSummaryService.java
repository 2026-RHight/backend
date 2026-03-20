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
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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

            YearMonth previousMonth = YearMonth.of(targetYear, targetMonth).minusMonths(1);
            int previousCompositeScore =
                    nvl(
                            performanceDashboardSummaryMapper.findCompositeScore(
                                    orgId,
                                    employeeId,
                                    previousMonth.getYear(),
                                    previousMonth.getMonthValue()));
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
        } catch (BadSqlGrammarException | DateTimeException ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.warn(
                    "성과 대시보드 요약 재계산을 건너뜁니다. 스키마 또는 HR facade 구성이 완전하지 않을 수 있습니다. employeeId={}, year={}, month={}",
                    employeeId,
                    targetYear,
                    targetMonth,
                    ex);
            return new PerformanceDashboardSummaryResponse(
                    false, targetYear, targetMonth, null, null, null, null, null);
        }
    }

    @Transactional
    public PerformanceDashboardSummaryResponse getCurrentMonthSummary(Long employeeId) {
        YearMonth currentMonth = YearMonth.now();
        try {
            return recalculate(employeeId, currentMonth.getYear(), currentMonth.getMonthValue());
        } catch (BadSqlGrammarException ex) {
            log.warn(
                    "성과 대시보드 요약 조회를 건너뜁니다. 스키마 또는 HR facade 구성이 완전하지 않을 수 있습니다. employeeId={}",
                    employeeId,
                    ex);
        }
        return new PerformanceDashboardSummaryResponse(
                false,
                currentMonth.getYear(),
                currentMonth.getMonthValue(),
                null,
                null,
                null,
                null,
                null);
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
            return new PerformanceDashboardSummaryResponse(
                    false, null, null, null, null, null, null, null);
        }
        return new PerformanceDashboardSummaryResponse(
                true,
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
