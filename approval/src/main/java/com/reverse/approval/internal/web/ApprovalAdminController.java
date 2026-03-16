package com.reverse.approval.internal.web;

import com.reverse.approval.internal.application.ApprovalService;
import com.reverse.approval.internal.dto.response.ApprovalVacationPageResponse;
import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/approval/admin")
public class ApprovalAdminController {

    private final ApprovalService approvalService;

    @Operation(summary = "휴가 신청 목록 조회 (관리자)")
    @GetMapping("/vacation-list")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasAnyRole('EVALUATOR','HR_ADMIN_MASTER','HR_ADMIN_BASIC','SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<ApprovalVacationPageResponse>> getVacationList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUser user) {
        ApprovalVacationPageResponse response =
                approvalService.getAdminVacationList(user.getEmployeeId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
