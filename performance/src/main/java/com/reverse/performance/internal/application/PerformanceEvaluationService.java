package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.request.PerformanceTeamEvaluationSubmitRequest;
import com.reverse.performance.internal.dto.request.TeamEvalRequest;
import com.reverse.performance.internal.dto.response.PerformancePeerReviewTargetResponse;
import com.reverse.performance.internal.dto.response.PerformanceTeamEvaluationAveragesResponse;
import com.reverse.performance.internal.dto.response.PerformanceTeamEvaluationTargetResponse;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import com.reverse.performance.internal.exception.PerformanceActionNotAllowedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceEvaluationService {

    private final PerformanceViewMapper performanceViewMapper;
    private final PerformanceService performanceService;

    public List<PerformanceTeamEvaluationTargetResponse> getTeamEvaluationTargets(Long employeeId) {
        return performanceViewMapper.findTeamEvaluationTargets(employeeId).stream()
                .map(row -> new PerformanceTeamEvaluationTargetResponse(
                        row.id(),
                        row.name(),
                        row.role(),
                        row.department(),
                        row.status(),
                        nvl(row.systemScore()),
                        nvd(row.peerReviewScore()),
                        new PerformanceTeamEvaluationAveragesResponse(
                                nvd(row.performanceAvg()),
                                nvd(row.attitudeAvg()),
                                nvd(row.collaborationAvg()),
                                nvd(row.creativityAvg())
                        )
                ))
                .toList();
    }

    public List<PerformancePeerReviewTargetResponse> getPeerReviewTargets(Long employeeId) {
        return performanceViewMapper.findPeerReviewTargets(employeeId);
    }

    @Transactional
    public void submitTeamEvaluation(Long evaluatorId, PerformanceTeamEvaluationSubmitRequest request) {
        if (request == null || request.appraiseeId() == null) {
            throw new PerformanceActionNotAllowedException("평가 대상이 필요합니다.");
        }

        performanceService.saveTeamEval(new TeamEvalRequest(
                evaluatorId,
                request.appraiseeId(),
                null,
                null,
                null,
                null,
                request.performanceScore(),
                request.performanceComment(),
                request.attitudeScore(),
                request.attitudeComment(),
                request.collaborationScore(),
                request.collaborationComment(),
                request.creativityScore(),
                request.creativityComment()
        ));
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private double nvd(Double value) {
        return value == null ? 0.0 : value;
    }
}
