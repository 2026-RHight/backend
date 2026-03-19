package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.request.PerformanceTeamEvaluationSubmitRequest;
import com.reverse.performance.internal.dto.request.TeamEvalRequest;
import com.reverse.performance.internal.dto.response.PerformancePeerReviewTargetResponse;
import com.reverse.performance.internal.dto.response.PerformanceTeamEvaluationAveragesResponse;
import com.reverse.performance.internal.dto.response.PerformanceTeamEvaluationTargetResponse;
import com.reverse.performance.internal.exception.PerformanceActionNotAllowedException;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
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
public class PerformanceEvaluationService {

    private final PerformanceHrMemberResolver performanceHrMemberResolver;
    private final PerformanceViewMapper performanceViewMapper;
    private final PerformanceService performanceService;

    public List<PerformanceTeamEvaluationTargetResponse> getTeamEvaluationTargets(Long employeeId) {
        List<PerformanceHrMemberResolver.OrganizationMemberSnapshot> members =
                performanceHrMemberResolver.getMyOrganizationMembers(employeeId).stream()
                        .filter(member -> !employeeId.equals(member.employeeId()))
                        .toList();
        if (members.isEmpty()) {
            return List.of();
        }

        Long orgId =
                members.stream()
                        .map(PerformanceHrMemberResolver.OrganizationMemberSnapshot::orgId)
                        .filter(id -> id != null)
                        .findFirst()
                        .orElse(null);
        Map<Long, PerformanceViewMapper.TeamEvaluationMetricRow> metricMap =
                performanceViewMapper
                        .findTeamEvaluationMetrics(
                                employeeId,
                                members.stream()
                                        .map(
                                                PerformanceHrMemberResolver
                                                                .OrganizationMemberSnapshot
                                                        ::employeeId)
                                        .toList(),
                                orgId)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        PerformanceViewMapper.TeamEvaluationMetricRow::employeeId,
                                        Function.identity()));

        return members.stream()
                .map(
                        member -> {
                            PerformanceViewMapper.TeamEvaluationMetricRow row =
                                    metricMap.get(member.employeeId());
                            return new PerformanceTeamEvaluationTargetResponse(
                                    member.employeeId(),
                                    member.employeeName(),
                                    defaultString(member.jobName(), "팀원"),
                                    defaultString(member.orgName(), "소속팀"),
                                    row == null ? "평가 대기" : row.status(),
                                    row == null ? 0 : nvl(row.systemScore()),
                                    row == null ? 0.0 : nvd(row.peerReviewScore()),
                                    new PerformanceTeamEvaluationAveragesResponse(
                                            row == null ? 0.0 : nvd(row.performanceAvg()),
                                            row == null ? 0.0 : nvd(row.attitudeAvg()),
                                            row == null ? 0.0 : nvd(row.collaborationAvg()),
                                            row == null ? 0.0 : nvd(row.creativityAvg())));
                        })
                .toList();
    }

    public List<PerformancePeerReviewTargetResponse> getPeerReviewTargets(Long employeeId) {
        List<PerformanceHrMemberResolver.OrganizationMemberSnapshot> members =
                performanceHrMemberResolver.getMyOrganizationMembers(employeeId).stream()
                        .filter(member -> !employeeId.equals(member.employeeId()))
                        .toList();
        if (members.isEmpty()) {
            return List.of();
        }

        Map<Long, Boolean> evaluatedMap =
                performanceViewMapper
                        .findPeerReviewTargetStates(
                                employeeId,
                                members.stream()
                                        .map(
                                                PerformanceHrMemberResolver
                                                                .OrganizationMemberSnapshot
                                                        ::employeeId)
                                        .toList())
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        PerformanceViewMapper.PeerReviewTargetStateRow::employeeId,
                                        PerformanceViewMapper.PeerReviewTargetStateRow::evaluated));

        return members.stream()
                .map(
                        member ->
                                new PerformancePeerReviewTargetResponse(
                                        member.employeeId(),
                                        member.employeeName(),
                                        defaultString(member.orgName(), "소속팀"),
                                        evaluatedMap.getOrDefault(member.employeeId(), false)))
                .toList();
    }

    @Transactional
    public void submitTeamEvaluation(
            Long evaluatorId, PerformanceTeamEvaluationSubmitRequest request) {
        if (request == null || request.appraiseeId() == null) {
            throw new PerformanceActionNotAllowedException("평가 대상이 필요합니다.");
        }

        performanceService.saveTeamEval(
                new TeamEvalRequest(
                        evaluatorId,
                        request.appraiseeId(),
                        null,
                        request.performanceScore(),
                        request.performanceComment(),
                        request.attitudeScore(),
                        request.attitudeComment(),
                        request.collaborationScore(),
                        request.collaborationComment(),
                        request.creativityScore(),
                        request.creativityComment()));
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private double nvd(Double value) {
        return value == null ? 0.0 : value;
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
