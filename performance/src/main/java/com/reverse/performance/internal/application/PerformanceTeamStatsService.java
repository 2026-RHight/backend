package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.response.PerformanceTeamStatChartItemResponse;
import com.reverse.performance.internal.dto.response.PerformanceTeamStatTaskResponse;
import com.reverse.performance.internal.dto.response.PerformanceTeamStatsMemberResponse;
import com.reverse.performance.internal.dto.response.PerformanceTeamStatsResponse;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceTeamStatsService {

    private final PerformanceHrMemberResolver performanceHrMemberResolver;
    private final PerformanceViewMapper performanceViewMapper;

    public PerformanceTeamStatsResponse getTeamStats(Long employeeId, String teamName) {
        List<PerformanceHrMemberResolver.OrganizationMemberSnapshot> organizationMembers =
                performanceHrMemberResolver.getMyOrganizationMembers(employeeId);
        List<String> teamOptions =
                organizationMembers.stream()
                        .map(PerformanceHrMemberResolver.OrganizationMemberSnapshot::orgName)
                        .filter(name -> name != null && !name.isBlank())
                        .distinct()
                        .sorted()
                        .toList();

        List<PerformanceHrMemberResolver.OrganizationMemberSnapshot> members =
                organizationMembers.stream()
                        .filter(
                                member ->
                                        blankToNull(teamName) == null
                                                || blankToNull(teamName).equals(member.orgName()))
                        .toList();
        if (members.isEmpty()) {
            return new PerformanceTeamStatsResponse(teamOptions, List.of());
        }

        List<Long> employeeIds =
                members.stream()
                        .map(PerformanceHrMemberResolver.OrganizationMemberSnapshot::employeeId)
                        .toList();
        Map<Long, PerformanceViewMapper.TeamStatsMetricRow> metricMap =
                performanceViewMapper.findTeamStatsMetrics(employeeIds).stream()
                        .collect(
                                Collectors.toMap(
                                        PerformanceViewMapper.TeamStatsMetricRow::employeeId,
                                        Function.identity(),
                                        (existing, replacement) -> replacement));
        Map<Long, List<PerformanceTeamStatTaskResponse>> taskMap =
                performanceViewMapper.findTeamStatsTasksByEmployeeIds(employeeIds).stream()
                        .collect(
                                Collectors.groupingBy(
                                        PerformanceViewMapper.PerformanceTeamStatsTaskRow
                                                ::employeeId,
                                        Collectors.collectingAndThen(
                                                Collectors.toList(),
                                                rows ->
                                                        rows.stream()
                                                                .sorted(
                                                                        Comparator.comparing(
                                                                                        PerformanceViewMapper
                                                                                                        .PerformanceTeamStatsTaskRow
                                                                                                ::createdAt)
                                                                                .reversed())
                                                                .limit(3)
                                                                .map(
                                                                        row ->
                                                                                new PerformanceTeamStatTaskResponse(
                                                                                        row
                                                                                                .performanceId(),
                                                                                        row.title(),
                                                                                        row
                                                                                                .status()))
                                                                .toList())));

        List<PerformanceTeamStatsMemberResponse> responses =
                members.stream()
                        .map(
                                member -> {
                                    PerformanceViewMapper.TeamStatsMetricRow row =
                                            metricMap.get(member.employeeId());
                                    return new PerformanceTeamStatsMemberResponse(
                                            member.employeeId(),
                                            member.employeeName(),
                                            defaultString(member.jobName(), "팀원"),
                                            defaultString(member.orgName(), "소속팀"),
                                            roundOneDecimal(
                                                    average(
                                                            row == null
                                                                    ? null
                                                                    : row.performanceAvg(),
                                                            row == null ? null : row.attitudeAvg(),
                                                            row == null
                                                                    ? null
                                                                    : row.collaborationAvg(),
                                                            row == null
                                                                    ? null
                                                                    : row.creativityAvg())),
                                            row == null ? 0 : nvl(row.systemScore()),
                                            List.of(
                                                    new PerformanceTeamStatChartItemResponse(
                                                            "업무 성과",
                                                            roundOneDecimal(
                                                                    row == null
                                                                            ? 0.0
                                                                            : nvd(
                                                                                    row
                                                                                            .performanceAvg()))),
                                                    new PerformanceTeamStatChartItemResponse(
                                                            "업무 태도",
                                                            roundOneDecimal(
                                                                    row == null
                                                                            ? 0.0
                                                                            : nvd(
                                                                                    row
                                                                                            .attitudeAvg()))),
                                                    new PerformanceTeamStatChartItemResponse(
                                                            "협업 능력",
                                                            roundOneDecimal(
                                                                    row == null
                                                                            ? 0.0
                                                                            : nvd(
                                                                                    row
                                                                                            .collaborationAvg()))),
                                                    new PerformanceTeamStatChartItemResponse(
                                                            "창의성",
                                                            roundOneDecimal(
                                                                    row == null
                                                                            ? 0.0
                                                                            : nvd(
                                                                                    row
                                                                                            .creativityAvg())))),
                                            taskMap.getOrDefault(member.employeeId(), List.of()));
                                })
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

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
