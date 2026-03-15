package com.reverse.hr.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.response.PageResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.hr.internal.application.OrganizationService;
import com.reverse.hr.internal.dto.response.EvidenceFileResponseDTO;
import com.reverse.hr.internal.dto.response.OrganizationMemberDetailResponseDTO;
import com.reverse.hr.internal.dto.response.OrganizationMemberResponseDTO;
import com.reverse.hr.internal.dto.response.OrganizationTreeNodeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/org")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/tree")
    @Operation(summary = "조직 트리 조회")
    public ApiResponse<List<OrganizationTreeNodeResponseDTO>> getOrganizationTree(
            @AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(organizationService.getOrganizationTree());
    }

    @GetMapping("/{orgId}/members")
    @Operation(summary = "조직 구성원 목록 조회")
    public ApiResponse<List<OrganizationMemberResponseDTO>> getOrganizationMembers(
            @AuthenticationPrincipal CustomUser user, @PathVariable Long orgId) {
        return ApiResponse.success(organizationService.getOrganizationMembers(orgId));
    }

    @GetMapping("/my/members")
    @Operation(summary = "내 조직 구성원 목록 조회")
    public ApiResponse<PageResponse<OrganizationMemberResponseDTO>> getMyOrganizationMembers(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) Long orgId,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        return ApiResponse.success(
                organizationService.getMyOrganizationMembers(
                        user.getEmployeeId(), orgId, page, 100));
    }

    @GetMapping("/members/{targetEmployeeId}/detail")
    @Operation(summary = "조직 구성원 상세 조회")
    @PreAuthorize("hasRole('EVALUATOR')")
    public ApiResponse<OrganizationMemberDetailResponseDTO> getOrganizationMemberDetail(
            @AuthenticationPrincipal CustomUser user, @PathVariable Long targetEmployeeId) {
        return ApiResponse.success(
                organizationService.getOrganizationMemberDetail(
                        user.getEmployeeId(), targetEmployeeId));
    }

    @GetMapping("/members/{targetEmployeeId}/skills/{skillId}/evidence")
    @Operation(summary = "조직 구성원 역량 증빙 조회")
    @PreAuthorize("hasRole('EVALUATOR')")
    public ApiResponse<EvidenceFileResponseDTO> getOrganizationMemberSkillEvidence(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long targetEmployeeId,
            @PathVariable Long skillId) {
        return ApiResponse.success(
                organizationService.getOrganizationMemberSkillEvidence(
                        user.getEmployeeId(), targetEmployeeId, skillId));
    }

    @GetMapping("/members/{targetEmployeeId}/careers/{careerId}/evidence")
    @Operation(summary = "조직 구성원 경력 증빙 조회")
    @PreAuthorize("hasRole('EVALUATOR')")
    public ApiResponse<EvidenceFileResponseDTO> getOrganizationMemberCareerEvidence(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long targetEmployeeId,
            @PathVariable Long careerId) {
        return ApiResponse.success(
                organizationService.getOrganizationMemberCareerEvidence(
                        user.getEmployeeId(), targetEmployeeId, careerId));
    }
}
