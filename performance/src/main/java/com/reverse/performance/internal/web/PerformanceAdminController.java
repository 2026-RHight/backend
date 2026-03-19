package com.reverse.performance.internal.web;

import com.reverse.core.exception.ForbiddenException;
import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.performance.internal.application.PerformanceAdminEvalService;
import com.reverse.performance.internal.application.PerformanceWeightService;
import com.reverse.performance.internal.dto.request.PerformanceWeightUpsertRequest;
import com.reverse.performance.internal.dto.response.AdminEvalDataResponse;
import com.reverse.performance.internal.dto.response.AdminEvalMemberResponse;
import com.reverse.performance.internal.dto.response.AdminEvalTeamResponse;
import com.reverse.performance.internal.dto.response.PerformanceWeightResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final PerformanceAdminEvalService performanceAdminEvalService;

    @Operation(summary = "관리자 팀 목록 조회")
    @GetMapping("/evaluation/teams")
    public ApiResponse<List<AdminEvalTeamResponse>> getAdminEvalTeams(
            @AuthenticationPrincipal CustomUser user) {
        validateAdmin(user);
        return ApiResponse.success(performanceAdminEvalService.getAdminEvalTeams());
    }

    @Operation(summary = "관리자 팀원 목록 조회")
    @GetMapping("/evaluation/teams/{orgId}/members")
    public ApiResponse<List<AdminEvalMemberResponse>> getAdminEvalMembers(
            @PathVariable Long orgId, @AuthenticationPrincipal CustomUser user) {
        validateAdmin(user);
        return ApiResponse.success(performanceAdminEvalService.getAdminEvalMembers(orgId));
    }

    @Operation(summary = "관리자 팀원 평가 상세 조회")
    @GetMapping("/evaluation/{employeeId}")
    public ApiResponse<AdminEvalDataResponse> getAdminEvalData(
            @PathVariable Long employeeId, @AuthenticationPrincipal CustomUser user) {
        validateAdmin(user);
        return ApiResponse.success(performanceAdminEvalService.getAdminEvalData(employeeId));
    }

    @Operation(summary = "관리자 인사고과 등급 저장")
    @PostMapping("/evaluation/{employeeId}")
    public ApiResponse<Void> saveAdminEvalScore(
            @PathVariable Long employeeId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUser user) {
        validateAdmin(user);
        performanceAdminEvalService.saveAdminFinalScore(
                employeeId, user.getEmployeeId(), body.get("grade"));
        return ApiResponse.success(null);
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
            throw new ForbiddenException("관리자 권한이 필요합니다.");
        }
    }
}
