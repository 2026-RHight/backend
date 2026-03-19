package com.reverse.performance.internal.web;

import com.reverse.core.exception.ForbiddenException;
import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.performance.internal.application.PerformanceEvaluationService;
import com.reverse.performance.internal.application.PerformanceWeightService;
import com.reverse.performance.internal.dto.request.PerformanceWeightUpsertRequest;
import com.reverse.performance.internal.dto.response.PerformanceTeamEvaluationTargetResponse;
import com.reverse.performance.internal.dto.response.PerformanceWeightResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/performance/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class PerformanceAdminController {

    private static final Set<String> ADMIN_ROLES =
            Set.of(
                    "ROLE_HR_ADMIN_MASTER",
                    "ROLE_HR_ADMIN_BASIC",
                    "ROLE_HR_ADMIN_PAYROLL",
                    "HR_ADMIN_MASTER",
                    "HR_ADMIN_BASIC",
                    "HR_ADMIN_PAYROLL",
                    "ADMIN");

    private final PerformanceWeightService performanceWeightService;
    private final PerformanceEvaluationService performanceEvaluationService;

    @Operation(summary = "관리자 팀 평가 대상 조회")
    @GetMapping("/evaluation/teams")
    public ApiResponse<List<PerformanceTeamEvaluationTargetResponse>> getEvaluationTeams(
            @AuthenticationPrincipal CustomUser user) {
        validateAdmin(user);
        return ApiResponse.success(
                performanceEvaluationService.getTeamEvaluationTargets(user.getEmployeeId()));
    }

    @Operation(summary = "관리자 성과 반영 비율 조회")
    @GetMapping("/weights")
    public ApiResponse<List<PerformanceWeightResponse>> getWeights(
            @AuthenticationPrincipal CustomUser user) {
        validateAdmin(user);
        return ApiResponse.success(performanceWeightService.getWeights());
    }

    @Operation(summary = "관리자 성과 반영 비율 저장")
    @PatchMapping("/weights")
    public ApiResponse<List<PerformanceWeightResponse>> upsertWeights(
            @RequestBody PerformanceWeightUpsertRequest request,
            @AuthenticationPrincipal CustomUser user) {
        validateAdmin(user);
        return ApiResponse.success(performanceWeightService.upsertWeights(request));
    }

    private void validateAdmin(CustomUser user) {
        boolean isAdmin =
                user != null
                        && user.getAuthorities().stream()
                                .anyMatch(
                                        authority ->
                                                ADMIN_ROLES.contains(authority.getAuthority()));
        if (!isAdmin) {
            throw new ForbiddenException("성과 반영 비율은 관리자만 관리할 수 있습니다.");
        }
    }
}
