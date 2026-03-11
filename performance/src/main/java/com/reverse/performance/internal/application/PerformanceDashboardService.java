package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.response.PerformanceDashboardResponse;
import com.reverse.performance.internal.dto.response.PerformanceDashboardSummaryResponse;
import com.reverse.performance.internal.dto.response.PerformanceFeedbackResponse;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceDashboardService {

    private final PerformanceDashboardSummaryService performanceDashboardSummaryService;
    private final PerformanceViewMapper performanceViewMapper;

    public PerformanceDashboardResponse getDashboard(Long employeeId) {
        PerformanceDashboardSummaryResponse summary =
                performanceDashboardSummaryService.getCurrentMonthSummary(employeeId);
        Integer pending = nvl(performanceViewMapper.countPendingApprovalItems(employeeId));
        List<PerformanceViewMapper.PerformanceTrendPoint> trendPoints =
                performanceViewMapper.findTrendPoints(employeeId);
        List<String> trendLabels =
                trendPoints.stream()
                        .map(PerformanceViewMapper.PerformanceTrendPoint::monthLabel)
                        .toList();
        List<Integer> trendScores = trendPoints.stream().map(point -> nvl(point.score())).toList();
        List<PerformanceFeedbackResponse> feedbacks =
                performanceViewMapper.findDashboardFeedbacks(employeeId);
        return new PerformanceDashboardResponse(
                pending, trendLabels, trendScores, summary, feedbacks);
    }

    public PerformanceDashboardSummaryResponse getDashboardSummary(Long employeeId) {
        return performanceDashboardSummaryService.getCurrentMonthSummary(employeeId);
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }
}
