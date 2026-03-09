package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.response.PerformanceTeamStatChartItemResponse;
import com.reverse.performance.internal.dto.response.PerformanceTeamStatTaskResponse;
import com.reverse.performance.internal.dto.response.PerformanceTeamStatsMemberResponse;
import com.reverse.performance.internal.dto.response.PerformanceTeamStatsResponse;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceTeamStatsService {

    private final PerformanceViewMapper performanceViewMapper;

    public PerformanceTeamStatsResponse getTeamStats(Long employeeId, String teamName) {
        List<String> teamOptions = performanceViewMapper.findManagedTeams(employeeId);
        List<PerformanceViewMapper.PerformanceTeamStatsMemberRow> members =
                performanceViewMapper.findTeamStatsMembers(employeeId, blankToNull(teamName));
        Map<Long, List<PerformanceTeamStatTaskResponse>> taskMap =
                performanceViewMapper.findTeamStatsTasks(employeeId, blankToNull(teamName)).stream()
                        .collect(Collectors.groupingBy(
                                PerformanceViewMapper.PerformanceTeamStatsTaskRow::employeeId,
                                Collectors.collectingAndThen(Collectors.toList(), rows -> rows.stream()
                                        .sorted(Comparator.comparing(PerformanceViewMapper.PerformanceTeamStatsTaskRow::createdAt).reversed())
                                        .limit(3)
                                        .map(row -> new PerformanceTeamStatTaskResponse(
                                                row.performanceId(),
                                                row.title(),
                                                row.status()
                                        ))
                                        .toList())
                        ));

        List<PerformanceTeamStatsMemberResponse> responses = members.stream()
                .map(row -> new PerformanceTeamStatsMemberResponse(
                        row.id(),
                        row.name(),
                        row.role(),
                        row.department(),
                        roundOneDecimal(average(row.performanceAvg(), row.attitudeAvg(), row.collaborationAvg(), row.creativityAvg())),
                        nvl(row.systemScore()),
                        List.of(
                                new PerformanceTeamStatChartItemResponse("업무 성과", roundOneDecimal(nvd(row.performanceAvg()))),
                                new PerformanceTeamStatChartItemResponse("업무 태도", roundOneDecimal(nvd(row.attitudeAvg()))),
                                new PerformanceTeamStatChartItemResponse("협업 능력", roundOneDecimal(nvd(row.collaborationAvg()))),
                                new PerformanceTeamStatChartItemResponse("창의성", roundOneDecimal(nvd(row.creativityAvg())))
                        ),
                        taskMap.getOrDefault(row.id(), List.of())
                ))
                .toList();

        return new PerformanceTeamStatsResponse(teamOptions, responses);
    }

    private double average(Double... values) {
        double sum = 0.0;
        int count = 0;
        for (Double value : values) {
            if (value != null) {
                sum += value;
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private double nvd(Double value) {
        return value == null ? 0.0 : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
