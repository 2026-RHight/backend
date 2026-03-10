package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.request.PeerReviewRequest;
import com.reverse.performance.internal.dto.request.PerformancePeerReviewSubmitRequest;
import com.reverse.performance.internal.exception.PerformanceActionNotAllowedException;
import com.reverse.performance.internal.exception.PerformanceNotFoundException;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformancePeerReviewService {

    private final PerformanceService performanceService;
    private final PerformanceViewMapper performanceViewMapper;

    @Transactional
    public void submitPeerReview(Long reviewerId, PerformancePeerReviewSubmitRequest request) {
        if (reviewerId == null || request == null || request.appraiseeId() == null) {
            throw new PerformanceActionNotAllowedException("평가 대상이 필요합니다.");
        }

        Long evalId = performanceViewMapper.findLatestEvaluationId(request.appraiseeId());
        if (evalId == null) {
            throw new PerformanceNotFoundException("평가 대상자의 평가 정보를 찾을 수 없습니다.");
        }
        LocalDate now = LocalDate.now();

        performanceService.savePeerReview(
                new PeerReviewRequest(
                        null,
                        evalId,
                        reviewerId,
                        request.communicationScore(),
                        request.solvingScore(),
                        request.responsibilityScore(),
                        request.teamContributionScore(),
                        request.cultureContributionScore(),
                        request.comment(),
                        now.getYear(),
                        now));
    }
}
