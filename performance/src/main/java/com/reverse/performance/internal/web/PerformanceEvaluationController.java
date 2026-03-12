package com.reverse.performance.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.performance.internal.application.PerformanceEvaluationService;
import com.reverse.performance.internal.application.PerformancePeerReviewService;
import com.reverse.performance.internal.application.PerformanceService;
import com.reverse.performance.internal.dto.request.EvalRequest;
import com.reverse.performance.internal.dto.request.PerformancePeerReviewSubmitRequest;
import com.reverse.performance.internal.dto.request.PerformanceTeamEvaluationSubmitRequest;
import com.reverse.performance.internal.dto.request.TeamEvalRequest;
import com.reverse.performance.internal.dto.response.PerformancePeerReviewTargetResponse;
import com.reverse.performance.internal.dto.response.PerformanceTeamEvaluationTargetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class PerformanceEvaluationController {

    private final PerformanceService performanceService;
    private final PerformanceEvaluationService performanceEvaluationService;
    private final PerformancePeerReviewService performancePeerReviewService;

    @Operation(summary = "평가 점수 등록")
    @PostMapping("/evaluation")
    public ApiResponse<Void> createEvaluation(
            @RequestBody EvalRequest dto, @AuthenticationPrincipal CustomUser user) {
        performanceService.saveEvaluation(user.getEmployeeId(), dto);
        return ApiResponse.success();
    }

    @Operation(summary = "동료 평가 등록")
    @PostMapping("/peer-review/raw")
    public ApiResponse<Void> createPeerReview(
            @RequestBody PerformancePeerReviewSubmitRequest request,
            @AuthenticationPrincipal CustomUser user) {
        performancePeerReviewService.submitPeerReview(user.getEmployeeId(), request);
        return ApiResponse.success();
    }

    @Operation(summary = "팀 평가 등록")
    @PostMapping("/team-evaluation/raw")
    public ApiResponse<Void> createTeamEval(
            @RequestBody TeamEvalRequest dto, @AuthenticationPrincipal CustomUser user) {
        performanceService.saveTeamEval(dto.withEvaluatorId(user.getEmployeeId()));
        return ApiResponse.success();
    }

    @Operation(summary = "팀 평가 대상 조회")
    @GetMapping("/team-evaluation/targets")
    public ApiResponse<List<PerformanceTeamEvaluationTargetResponse>> teamEvaluationTargets(
            @AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(
                performanceEvaluationService.getTeamEvaluationTargets(user.getEmployeeId()));
    }

    @Operation(summary = "팀 평가 화면 결과 저장")
    @PostMapping("/team-evaluation")
    public ApiResponse<Void> submitTeamEvaluation(
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestBody PerformanceTeamEvaluationSubmitRequest request) {
        performanceEvaluationService.submitTeamEvaluation(user.getEmployeeId(), request);
        return ApiResponse.success();
    }

    @Operation(summary = "동료 평가 대상 조회")
    @GetMapping("/peer-review/targets")
    public ApiResponse<List<PerformancePeerReviewTargetResponse>> peerReviewTargets(
            @AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(
                performanceEvaluationService.getPeerReviewTargets(user.getEmployeeId()));
    }

    @Operation(summary = "동료 평가 화면 결과 저장")
    @PostMapping("/peer-review")
    public ApiResponse<Void> submitPeerReview(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody PerformancePeerReviewSubmitRequest request) {
        performancePeerReviewService.submitPeerReview(user.getEmployeeId(), request);
        return ApiResponse.success();
    }
}
