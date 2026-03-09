package com.reverse.performance.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.performance.internal.application.PerformanceInquiryService;
import com.reverse.performance.internal.application.PerformanceService;
import com.reverse.performance.internal.domain.Status;
import com.reverse.performance.internal.domain.WorkItem;
import com.reverse.performance.internal.dto.request.PerformanceResultUpdateRequest;
import com.reverse.performance.internal.dto.response.EvaluatorPerformanceResponse;
import com.reverse.performance.internal.dto.response.MyPerformanceResponse;
import com.reverse.performance.internal.dto.response.PerformanceInquiryItemResponse;
import com.reverse.performance.internal.dto.response.PersonalPerformanceResponse;
import com.reverse.performance.internal.dto.response.TeamPerformanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class PerformanceInquiryController {

    private final PerformanceService performanceService;
    private final PerformanceInquiryService performanceInquiryService;

    @Operation(summary = "내 성과 목록 조회")
    @GetMapping("/my-performances")
    public ApiResponse<List<MyPerformanceResponse>> myPerformance(
            @AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(performanceService.findAllPerformance(user.getEmployeeId()));
    }

    @Operation(summary = "평가자 기준 성과 목록 조회")
    @GetMapping("/evaluator/performances")
    public ApiResponse<List<EvaluatorPerformanceResponse>> evaluatorPerformances(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) Long appraiseeId,
            @RequestParam(required = false) Status status) {
        return ApiResponse.success(
                performanceService.findAllEvaluatorPerformance(
                        user.getEmployeeId(),
                        appraiseeId,
                        status
                )
        );
    }

    @Operation(summary = "개인 성과 조회")
    @GetMapping("/personal-performance")
    public ApiResponse<List<PersonalPerformanceResponse>> myPersonalPerformance(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) WorkItem workItem) {
        return ApiResponse.success(
                performanceService.findAllPersonalPerformance(user.getEmployeeId(), workItem)
        );
    }

    @Operation(summary = "팀 성과 조회")
    @GetMapping("/team-performance")
    public ApiResponse<List<TeamPerformanceResponse>> myTeamPerformance(
            @AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(
                performanceService.findallTeamPerformance(user.getEmployeeId())
        );
    }

    @Operation(summary = "성과 조회 화면 목록 조회")
    @GetMapping("/inquiry")
    public ApiResponse<List<PerformanceInquiryItemResponse>> inquiry(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) Long employeeId) {
        return ApiResponse.success(
                performanceInquiryService.getInquiryItems(
                        user.getEmployeeId(),
                        employeeId,
                        isAdmin(user)
                )
        );
    }

    @Operation(summary = "성과 결과 등록")
    @PatchMapping(value = "/result/{performanceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> updateResult(
            @PathVariable Long performanceId,
            @RequestPart("request") PerformanceResultUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        performanceInquiryService.updateResult(performanceId, request, files == null ? List.of() : files);
        return ApiResponse.success();
    }

    private boolean isAdmin(CustomUser user) {
        return user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().contains("ADMIN"));
    }
}
