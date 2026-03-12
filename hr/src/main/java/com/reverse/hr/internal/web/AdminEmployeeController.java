package com.reverse.hr.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.response.PageResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.hr.internal.application.AdminEmployeeService;
import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.SensitiveFieldType;
import com.reverse.hr.internal.dto.request.AdminEmployeeCreateRequestDTO;
import com.reverse.hr.internal.dto.request.AdminEmployeeRevealRequestDTO;
import com.reverse.hr.internal.dto.response.AdminEmployeeCreateResponseDTO;
import com.reverse.hr.internal.dto.response.AdminEmployeeDetailResponseDTO;
import com.reverse.hr.internal.dto.response.AdminEmployeeListItemResponseDTO;
import com.reverse.hr.internal.dto.response.AdminSensitiveValueResponseDTO;
import com.reverse.hr.internal.dto.response.EvidenceFileResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/employees")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class AdminEmployeeController {

    private final AdminEmployeeService adminEmployeeService;

    @PostMapping
    @Operation(summary = "인사관리자 신규 사원 등록")
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER','HR_ADMIN_BASIC')")
    public ApiResponse<AdminEmployeeCreateResponseDTO> createEmployee(
            @Valid @RequestBody AdminEmployeeCreateRequestDTO request) {
        return ApiResponse.success(adminEmployeeService.createEmployee(request));
    }

    @GetMapping
    @Operation(summary = "인사관리자 전 사원 목록 조회")
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER','HR_ADMIN_BASIC','HR_ADMIN_PAYROLL')")
    public ApiResponse<PageResponse<AdminEmployeeListItemResponseDTO>> getEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) EmployeeState employeeState,
            @RequestParam(required = false) EmployType employType,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        return ApiResponse.success(
                adminEmployeeService.getEmployees(
                        keyword, orgId, employeeState, employType, page, 10));
    }

    @GetMapping("/{employeeId}")
    @Operation(summary = "인사관리자 사원 상세 조회")
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER','HR_ADMIN_BASIC','HR_ADMIN_PAYROLL')")
    public ApiResponse<AdminEmployeeDetailResponseDTO> getEmployeeDetail(
            @PathVariable Long employeeId) {
        return ApiResponse.success(adminEmployeeService.getEmployeeDetail(employeeId));
    }

    @GetMapping("/{employeeId}/skills/{skillId}/evidence")
    @Operation(summary = "인사관리자 사원 역량 증빙 조회")
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER','HR_ADMIN_BASIC','HR_ADMIN_PAYROLL')")
    public ApiResponse<EvidenceFileResponseDTO> getEmployeeSkillEvidence(
            @PathVariable Long employeeId, @PathVariable Long skillId) {
        return ApiResponse.success(
                adminEmployeeService.getEmployeeSkillEvidence(employeeId, skillId));
    }

    @GetMapping("/{employeeId}/careers/{careerId}/evidence")
    @Operation(summary = "인사관리자 사원 경력 증빙 조회")
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER','HR_ADMIN_BASIC','HR_ADMIN_PAYROLL')")
    public ApiResponse<EvidenceFileResponseDTO> getEmployeeCareerEvidence(
            @PathVariable Long employeeId, @PathVariable Long careerId) {
        return ApiResponse.success(
                adminEmployeeService.getEmployeeCareerEvidence(employeeId, careerId));
    }

    @PostMapping("/{employeeId}/sensitive/reveal")
    @Operation(summary = "인사관리자 민감정보 조회(실값)")
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER','HR_ADMIN_PAYROLL')")
    public ApiResponse<AdminSensitiveValueResponseDTO> revealSensitiveField(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long employeeId,
            @Valid @RequestBody AdminEmployeeRevealRequestDTO request) {
        return ApiResponse.success(
                adminEmployeeService.revealSensitiveField(
                        user.getEmployeeId(),
                        employeeId,
                        SensitiveFieldType.from(request.fieldType()),
                        request.reason()));
    }
}
