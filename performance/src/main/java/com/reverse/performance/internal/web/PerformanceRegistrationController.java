package com.reverse.performance.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.performance.internal.application.PerformanceRegistrationService;
import com.reverse.performance.internal.application.PerformanceService;
import com.reverse.performance.internal.dto.request.AttachmentRequest;
import com.reverse.performance.internal.dto.request.PerformanceCreateDTO;
import com.reverse.performance.internal.dto.request.PerformanceRegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class PerformanceRegistrationController {

    private final PerformanceService performanceService;
    private final PerformanceRegistrationService performanceRegistrationService;

    @Operation(summary = "기본 성과 등록")
    @PostMapping
    public ApiResponse<Void> createPerformance(
            @RequestBody PerformanceCreateDTO dto,
            @AuthenticationPrincipal CustomUser user) {
        performanceService.save(dto.withEmployeeId(user.getEmployeeId()));
        return ApiResponse.success();
    }

    @Operation(summary = "성과 첨부 메타데이터 저장")
    @PostMapping("/report")
    public ApiResponse<Void> createReport(@RequestBody AttachmentRequest dto) {
        performanceService.saveAttachment(dto);
        return ApiResponse.success();
    }

    @Operation(summary = "성과 등록 화면용 성과 등록")
    @PostMapping("/register")
    public ApiResponse<Void> register(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody PerformanceRegistrationRequest request) {
        performanceRegistrationService.register(user.getEmployeeId(), request);
        return ApiResponse.success();
    }
}
