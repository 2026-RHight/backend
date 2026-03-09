package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.response.PerformanceFeedbackResponse;
import com.reverse.performance.internal.dto.response.PerformanceMonthlyDetailItemResponse;
import com.reverse.performance.internal.dto.response.PerformanceMonthlyStatResponse;
import com.reverse.performance.internal.dto.response.PerformanceMonthlyResponse;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceMonthlyService {

    private final PerformanceViewMapper performanceViewMapper;

    public PerformanceMonthlyResponse getMonthly(
            Long viewerEmployeeId, Long targetEmployeeId, boolean isAdmin, Integer monthOffset) {
        YearMonth targetMonth = YearMonth.now().plusMonths(monthOffset == null ? 0L : monthOffset.longValue());
        List<PerformanceViewMapper.PerformanceMonthlyPoint> myPoints =
                performanceViewMapper.findMonthlyMyScores(viewerEmployeeId, targetEmployeeId, isAdmin);
        List<PerformanceViewMapper.PerformanceMonthlyPoint> teamPoints =
                performanceViewMapper.findMonthlyTeamScores(viewerEmployeeId, targetEmployeeId, isAdmin);

        List<String> chartLabels = buildRecentMonthLabels(targetMonth);
        List<Integer> myScores = mapScores(chartLabels, myPoints);
        List<Integer> teamScores = mapScores(chartLabels, teamPoints);

        int currentIndex = Math.max(0, chartLabels.size() - 1);
        int currentMyScore = valueAt(myScores, currentIndex);
        int currentTeamScore = valueAt(teamScores, currentIndex);
        int prevScore = currentIndex > 0 ? valueAt(myScores, currentIndex - 1) : 0;
        double changeRate = prevScore == 0 ? 0.0 : ((double) (currentMyScore - prevScore) / prevScore) * 100.0;

        List<PerformanceMonthlyStatResponse> stats = List.of(
                new PerformanceMonthlyStatResponse("개인 업무 달성률", currentMyScore + "%"),
                new PerformanceMonthlyStatResponse("팀 업무 달성률", currentTeamScore + "%"),
                new PerformanceMonthlyStatResponse("전월 대비 점수 변화율", String.format("%+.1f%%", changeRate)),
                new PerformanceMonthlyStatResponse("종합 점수", currentMyScore + "점")
        );

        List<PerformanceMonthlyDetailItemResponse> detailItems =
                performanceViewMapper.findMonthlyDetailItems(
                        viewerEmployeeId,
                        targetEmployeeId,
                        isAdmin,
                        targetMonth.getYear(),
                        targetMonth.getMonthValue()
                ).stream().map(row -> new PerformanceMonthlyDetailItemResponse(
                        row.id(),
                        row.type(),
                        row.title(),
                        row.date(),
                        nvl(row.progress()),
                        nvl(row.score()),
                        row.description(),
                        row.achievement(),
                        row.feedbackText() == null || row.feedbackText().isBlank()
                                ? List.of()
                                : List.of(new PerformanceFeedbackResponse(
                                        row.id(),
                                        row.feedbackText(),
                                        row.feedbackAuthor() == null ? "시스템" : row.feedbackAuthor(),
                                        row.feedbackDate(),
                                        row.id(),
                                        row.description()
                                ))
                )).toList();

        return new PerformanceMonthlyResponse(
                targetMonth.getYear(),
                targetMonth.getMonthValue(),
                stats,
                chartLabels,
                myScores,
                teamScores,
                detailItems
        );
    }

    private List<String> buildRecentMonthLabels(YearMonth targetMonth) {
        List<String> labels = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            labels.add(targetMonth.minusMonths(i).getMonthValue() + "월");
        }
        return labels;
    }

    private List<Integer> mapScores(
            List<String> chartLabels, List<PerformanceViewMapper.PerformanceMonthlyPoint> points) {
        List<Integer> scores = new ArrayList<>();
        for (String label : chartLabels) {
            Integer month = Integer.valueOf(label.replace("월", ""));
            Integer score = points.stream()
                    .filter(point -> point.scoreMonth() != null && point.scoreMonth().equals(month))
                    .map(PerformanceViewMapper.PerformanceMonthlyPoint::score)
                    .reduce((first, second) -> second)
                    .orElse(0);
            scores.add(nvl(score));
        }
        return scores;
    }

    private int valueAt(List<Integer> values, int index) {
        if (index < 0 || index >= values.size()) {
            return 0;
        }
        return nvl(values.get(index));
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }
}
