package com.reverse.performance.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.performance.internal.application.PerformanceApprovalService;
import com.reverse.performance.internal.application.PerformanceService;
import com.reverse.performance.internal.dto.request.PerformanceApprovalActionRequest;
import com.reverse.performance.internal.dto.response.AppraiseePerformance;
import com.reverse.performance.internal.dto.response.PerformanceApprovalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class PerformanceApprovalController {

    private final PerformanceService performanceService;
    private final PerformanceApprovalService performanceApprovalService;

    @Operation(summary = "승인 대기 성과 조회")
    @GetMapping("/waiting")
    public ApiResponse<List<AppraiseePerformance>> checkWaitingPerformance(
            @AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(
                performanceService.findWaitingPerformance(user.getEmployeeId())
        );
    }

    @Operation(summary = "승인 화면 목록 조회")
    @GetMapping("/approvals")
    public ApiResponse<PerformanceApprovalResponse> approvals(
            @AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(
                performanceApprovalService.getApprovalItems(user.getEmployeeId())
        );
    }

    @Operation(summary = "성과 확정 처리")
    @PatchMapping("/confirm/{performanceId}")
    public ApiResponse<Void> confirmPerformance(
            @PathVariable Long performanceId,
            @AuthenticationPrincipal CustomUser user) {
        performanceService.updateConfirm(performanceId, user.getEmployeeId());
        return ApiResponse.success();
    }

    @Operation(summary = "성과 승인 처리")
    @PostMapping("/approvals/{performanceId}/approve")
    public ApiResponse<Void> approve(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long performanceId,
            @RequestBody(required = false) PerformanceApprovalActionRequest request) {
        performanceApprovalService.approvePerformance(
                user.getEmployeeId(),
                performanceId,
                request == null ? new PerformanceApprovalActionRequest(null) : request
        );
        return ApiResponse.success();
    }

    @Operation(summary = "성과 반려 처리")
    @PostMapping("/approvals/{performanceId}/reject")
    public ApiResponse<Void> reject(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long performanceId,
            @RequestBody(required = false) PerformanceApprovalActionRequest request) {
        performanceApprovalService.rejectPerformance(
                user.getEmployeeId(),
                performanceId,
                request == null ? new PerformanceApprovalActionRequest(null) : request
        );
        return ApiResponse.success();
    }
}
