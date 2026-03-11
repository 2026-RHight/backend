package com.reverse.performance.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.performance.internal.application.PerformanceDashboardService;
import com.reverse.performance.internal.application.PerformanceService;
import com.reverse.performance.internal.dto.response.Dashboard;
import com.reverse.performance.internal.dto.response.PerformanceDashboardResponse;
import com.reverse.performance.internal.dto.response.PerformanceDashboardSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class PerformanceDashboardController {

    private final PerformanceService performanceService;
    private final PerformanceDashboardService performanceDashboardService;

    @Operation(summary = "성과 대시보드 목록 조회")
    @GetMapping
    public ApiResponse<List<Dashboard>> myInformation(@AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(performanceService.findDashboardInfo(user.getEmployeeId()));
    }

    @Operation(summary = "평가자 대기 건수 조회")
    @GetMapping("/evaluator")
    public ApiResponse<Long> evaluatorInformation(@AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(
                performanceService.countEvaluatorPendingDashboard(user.getEmployeeId()));
    }

    @Operation(summary = "대시보드 화면 요약 조회")
    @GetMapping("/dashboard")
    public ApiResponse<PerformanceDashboardResponse> dashboard(
            @AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(performanceDashboardService.getDashboard(user.getEmployeeId()));
    }

    @Operation(summary = "대시보드 요약 스냅샷 조회")
    @GetMapping("/dashboard/summary")
    public ApiResponse<PerformanceDashboardSummaryResponse> dashboardSummary(
            @AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(
                performanceDashboardService.getDashboardSummary(user.getEmployeeId()));
    }
}
