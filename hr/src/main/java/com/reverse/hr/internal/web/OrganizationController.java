package com.reverse.hr.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.hr.internal.application.OrganizationService;
import com.reverse.hr.internal.dto.response.OrganizationMemberResponseDTO;
import com.reverse.hr.internal.dto.response.OrganizationTreeNodeResponseDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/org")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/tree")
    public ApiResponse<List<OrganizationTreeNodeResponseDTO>> getOrganizationTree(@AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(organizationService.getOrganizationTree());
    }

    @GetMapping("/{orgId}/members")
    @PreAuthorize("hasRole('EVALUATOR')")
    public ApiResponse<List<OrganizationMemberResponseDTO>> getOrganizationMembers(@AuthenticationPrincipal CustomUser user, @PathVariable Long orgId) {
        return ApiResponse.success(organizationService.getOrganizationMembers(user.getEmployeeId(), orgId));
    }

    @GetMapping("/my/members")
    @PreAuthorize("hasRole('EVALUATOR')")
    public ApiResponse<List<OrganizationMemberResponseDTO>> getMyOrganizationMembers(@AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(organizationService.getMyOrganizationMembers(user.getEmployeeId()));
    }
}
