package com.reverse.hr.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.response.PageResponse;
import com.reverse.hr.internal.application.HrChangeService;
import com.reverse.hr.internal.domain.enums.HrEventType;
import com.reverse.hr.internal.dto.request.HrChangeUpdateRequestDTO;
import com.reverse.hr.internal.dto.response.HrChangeCurrentInfoResponseDTO;
import com.reverse.hr.internal.dto.response.HrChangeEmployeeSearchItemResponseDTO;
import com.reverse.hr.internal.dto.response.HrChangeEventResponseDTO;
import com.reverse.hr.internal.dto.response.HrChangeOptionsResponseDTO;
import com.reverse.hr.internal.dto.response.HrChangeUpdateResponseDTO;
import com.reverse.hr.internal.dto.response.OrganizationTreeNodeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/hr-change")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@PreAuthorize("hasRole('HR_ADMIN_MASTER')")
public class HrChangeController {

    private final HrChangeService hrChangeService;

    @GetMapping("/org-tree")
    @Operation(summary = "인사변경 대상 선택용 조직 트리 조회")
    public ApiResponse<List<OrganizationTreeNodeResponseDTO>> getOrgTree() {
        return ApiResponse.success(hrChangeService.getOrganizationTree());
    }

    @GetMapping("/employees/search")
    @Operation(summary = "인사변경 대상 사원 검색")
    public ApiResponse<PageResponse<HrChangeEmployeeSearchItemResponseDTO>> searchEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orgId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        return ApiResponse.success(hrChangeService.searchEmployees(keyword, orgId, page, size));
    }

    @GetMapping("/employees/{employeeId}/current")
    @Operation(summary = "인사변경 대상 사원 현재 인사 정보 조회")
    public ApiResponse<HrChangeCurrentInfoResponseDTO> getCurrentInfo(
            @PathVariable Long employeeId) {
        return ApiResponse.success(hrChangeService.getCurrentInfo(employeeId));
    }

    @GetMapping("/options")
    @Operation(summary = "인사변경 폼 옵션 조회")
    public ApiResponse<HrChangeOptionsResponseDTO> getOptions() {
        return ApiResponse.success(hrChangeService.getOptions());
    }

    @PatchMapping("/employees/{employeeId}")
    @Operation(summary = "사원 인사 정보 변경")
    public ApiResponse<HrChangeUpdateResponseDTO> updateEmployeeHrInfo(
            @PathVariable Long employeeId, @Valid @RequestBody HrChangeUpdateRequestDTO request) {
        return ApiResponse.success(hrChangeService.updateEmployeeHrInfo(employeeId, request));
    }

    @PutMapping("/employees/{employeeId}")
    @Operation(summary = "사원 인사 정보 변경(호환)")
    public ApiResponse<HrChangeUpdateResponseDTO> updateEmployeeHrInfoByPut(
            @PathVariable Long employeeId, @Valid @RequestBody HrChangeUpdateRequestDTO request) {
        return ApiResponse.success(hrChangeService.updateEmployeeHrInfo(employeeId, request));
    }

    @GetMapping("/events")
    @Operation(summary = "인사 변경 이력 조회")
    public ApiResponse<PageResponse<HrChangeEventResponseDTO>> getEvents(
            @RequestParam(required = false) HrEventType eventType,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate toDate,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        return ApiResponse.success(
                hrChangeService.getHrChangeEvents(
                        eventType, employeeId, fromDate, toDate, page, size));
    }
}
