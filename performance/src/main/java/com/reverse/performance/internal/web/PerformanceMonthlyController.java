package com.reverse.performance.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.performance.internal.application.PerformanceMonthlyService;
import com.reverse.performance.internal.application.PerformanceService;
import com.reverse.performance.internal.application.PerformanceTeamStatsService;
import com.reverse.performance.internal.dto.request.MonthlyScoreCreateRequest;
import com.reverse.performance.internal.dto.response.MonthlyResponse;
import com.reverse.performance.internal.dto.response.PerformanceMonthlyResponse;
import com.reverse.performance.internal.dto.response.PerformanceTeamStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class PerformanceMonthlyController {

    private final PerformanceService performanceService;
    private final PerformanceMonthlyService performanceMonthlyService;
    private final PerformanceTeamStatsService performanceTeamStatsService;

    @Operation(summary = "월별 성과 조회")
    @GetMapping("/monthly")
    public ApiResponse<List<MonthlyResponse>> myMonthlyPerformance(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(name = "offset", required = false, defaultValue = "0") Integer monthOffset) {
        return ApiResponse.success(
                performanceService.findMonthlyPerformance(user.getEmployeeId(), monthOffset)
        );
    }

    @Operation(summary = "월별 점수 생성")
    @PostMapping("/monthly/score")
    public ApiResponse<Void> createMonthlyScore(
            @RequestBody MonthlyScoreCreateRequest dto,
            @AuthenticationPrincipal CustomUser user) {
        performanceService.saveMonthlyScore(dto.withEmployeeId(user.getEmployeeId()));
        return ApiResponse.success();
    }

    @Operation(summary = "월별 점수 재계산")
    @PatchMapping("/monthly/score")
    public ApiResponse<Void> recalculateMonthlyScore(
            @RequestBody MonthlyScoreCreateRequest dto,
            @AuthenticationPrincipal CustomUser user) {
        performanceService.saveMonthlyScore(dto.withEmployeeId(user.getEmployeeId()));
        return ApiResponse.success();
    }

    @Operation(summary = "월간 리포트 화면 조회")
    @GetMapping("/monthly/report")
    public ApiResponse<PerformanceMonthlyResponse> monthly(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset) {
        return ApiResponse.success(
                performanceMonthlyService.getMonthly(
                        user.getEmployeeId(),
                        employeeId,
                        isAdmin(user),
                        offset
                )
        );
    }

    @Operation(summary = "팀 통계 화면 조회")
    @GetMapping("/team-stats")
    public ApiResponse<PerformanceTeamStatsResponse> teamStats(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) String team) {
        return ApiResponse.success(
                performanceTeamStatsService.getTeamStats(user.getEmployeeId(), team)
        );
    }

    private boolean isAdmin(CustomUser user) {
        return user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().contains("ADMIN"));
    }
}
