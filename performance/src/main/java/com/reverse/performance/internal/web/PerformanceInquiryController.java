package com.reverse.performance.internal.web;

import com.reverse.core.exception.BadRequestException;
import com.reverse.core.exception.ForbiddenException;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class PerformanceInquiryController {

    private static final Set<String> ADMIN_ROLES =
            Set.of("ROLE_HR_ADMIN_MASTER", "ROLE_HR_ADMIN_BASIC", "ROLE_HR_ADMIN_PAYROLL");

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
                        user.getEmployeeId(), appraiseeId, status));
    }

    @Operation(summary = "개인 성과 조회")
    @GetMapping("/personal-performance")
    public ApiResponse<List<PersonalPerformanceResponse>> myPersonalPerformance(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) WorkItem workItem) {
        return ApiResponse.success(
                performanceService.findAllPersonalPerformance(user.getEmployeeId(), workItem));
    }

    @Operation(summary = "팀 성과 조회")
    @GetMapping("/team-performance")
    public ApiResponse<List<TeamPerformanceResponse>> myTeamPerformance(
            @AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(performanceService.findallTeamPerformance(user.getEmployeeId()));
    }

    @Operation(summary = "성과 조회 화면 목록 조회")
    @GetMapping("/inquiry")
    public ApiResponse<List<PerformanceInquiryItemResponse>> inquiry(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(name = "targetEmployeeId", required = false) Long targetEmployeeId) {
        return ApiResponse.success(
                performanceInquiryService.getInquiryItems(
                        user.getEmployeeId(),
                        resolveTargetEmployeeId(user, targetEmployeeId),
                        isAdmin(user)));
    }

    @Operation(
            summary = "성과 결과 등록",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            required = true,
                            content = {
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                PerformanceResultUpdateRequest
                                                                        .class)),
                                @Content(
                                        mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                PerformanceResultUpdateRequest
                                                                        .class))
                            }))
    @PostMapping(value = "/result/{performanceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Void> updateResult(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long performanceId,
            @Valid @RequestBody PerformanceResultUpdateRequest request) {
        performanceInquiryService.updateResult(user.getEmployeeId(), performanceId, request);
        return ApiResponse.success();
    }

    @Operation(summary = "성과 결과 등록(첨부 포함)")
    @PostMapping(value = "/result/{performanceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> updateResultWithAttachments(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long performanceId,
            @Valid @RequestPart(value = "request", required = false)
                    PerformanceResultUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        if (request == null) {
            throw new BadRequestException("성과 결과 등록 요청이 비어 있습니다.");
        }
        performanceInquiryService.updateResult(user.getEmployeeId(), performanceId, request, files);
        return ApiResponse.success();
    }

    private boolean isAdmin(CustomUser user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> ADMIN_ROLES.contains(auth.getAuthority()));
    }

    private Long resolveTargetEmployeeId(CustomUser user, Long targetEmployeeId) {
        if (targetEmployeeId == null) {
            return isAdmin(user) || isEvaluator(user) ? null : user.getEmployeeId();
        }
        if (targetEmployeeId.equals(user.getEmployeeId())) {
            return targetEmployeeId;
        }
        if (!isAdmin(user) && !isEvaluator(user)) {
            throw new ForbiddenException("FORBIDDEN", "다른 직원의 성과를 조회할 권한이 없습니다.");
        }
        return targetEmployeeId;
    }

    private boolean isEvaluator(CustomUser user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_EVALUATOR".equals(auth.getAuthority()));
    }
}
