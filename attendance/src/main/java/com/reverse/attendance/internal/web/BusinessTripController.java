package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.BusinessTripService;
import com.reverse.attendance.internal.dto.request.BusinessTripApplyRequest;
import com.reverse.attendance.internal.dto.request.BusinessTripProcessRequest;
import com.reverse.attendance.internal.dto.response.BusinessTripResponse;
import com.reverse.core.response.PageResponse;
import com.reverse.core.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business-trips")
@RequiredArgsConstructor
public class BusinessTripController {

    private static final String BUSINESS_TRIP_SELF_SERVICE_AUTH =
            "hasAnyRole('EVALUATOR', 'EVALUATEE', 'HR_ADMIN_MASTER', "
                    + "'HR_ADMIN_PAYROLL', 'HR_ADMIN_BASIC', 'SYSTEM_ADMIN')";
    private static final String BUSINESS_TRIP_APPROVER_AUTH =
            "hasAnyRole('EVALUATOR', 'HR_ADMIN_MASTER', 'HR_ADMIN_BASIC', 'SYSTEM_ADMIN')";

    private final BusinessTripService businessTripService;

    // 사용자 : 내 신청 내역 조회
    @Operation(summary = "내 출장/외근 신청 내역 조회", description = "사용자 본인의 출장 및 외근 신청 내역을 조회합니다.")
    @GetMapping("/my-requests")
    @PreAuthorize(BUSINESS_TRIP_SELF_SERVICE_AUTH)
    public ResponseEntity<PageResponse<BusinessTripResponse>> getMyTrips(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(businessTripService.getMyTrips(user.getEmployeeId(), page, size));
    }

    // 사용자 : 내 신청 건수 상태별 요약
    @Operation(summary = "출장/외근 상태별 건수 요약", description = "사용자 본인의 출장 및 외근 신청 건수를 상태별로 요약합니다.")
    @GetMapping("/status-counts")
    @PreAuthorize(BUSINESS_TRIP_SELF_SERVICE_AUTH)
    public ResponseEntity<com.reverse.attendance.internal.dto.response.RequestStatusCountResponse>
            getMyRequestStatusCounts(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(
                businessTripService.getMyRequestStatusCounts(user.getEmployeeId()));
    }

    // 사용자 : 외근/출장 신청
    @Operation(summary = "외근/출장 신청", description = "사용자가 새로운 외근 및 출장을 신청합니다.")
    @PostMapping("/apply")
    @PreAuthorize(BUSINESS_TRIP_SELF_SERVICE_AUTH)
    public ResponseEntity<String> applyTrip(
            @Valid @RequestBody BusinessTripApplyRequest request,
            @AuthenticationPrincipal CustomUser user) {
        businessTripService.applyBusinessTrip(request, user.getEmployeeId());
        return ResponseEntity.ok("외근/출장 신청이 완료되었습니다.");
    }

    @Operation(summary = "외근/출장 신청 취소", description = "결재 대기 중인 외근 및 출장 신청을 취소합니다.")
    @PutMapping("/{tripId}/cancel")
    @PreAuthorize(BUSINESS_TRIP_SELF_SERVICE_AUTH)
    public ResponseEntity<String> cancelTrip(
            @PathVariable Long tripId, @AuthenticationPrincipal CustomUser user) {
        businessTripService.cancelTrip(tripId, user.getEmployeeId());
        return ResponseEntity.ok("신청이 취소되었습니다.");
    }

    // 관리자 : 전체 리스트 조회
    @Operation(summary = "모든 출장/외근 내역 조회 (관리자)", description = "관리자가 모든 직원의 출장 및 외근 내역을 조회합니다.")
    @PreAuthorize(BUSINESS_TRIP_APPROVER_AUTH)
    @GetMapping("/admin/requests")
    public ResponseEntity<PageResponse<BusinessTripResponse>> getAllTrips(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(businessTripService.getAllTrips(status, page, size));
    }

    // 관리자 : 승인/반려 결재
    @Operation(summary = "출장/외근 결재 (관리자)", description = "관리자가 직원의 출장 및 외근 신청을 승인하거나 반려합니다.")
    @PreAuthorize(BUSINESS_TRIP_APPROVER_AUTH)
    @PutMapping("/admin/process")
    public ResponseEntity<String> processTrip(@RequestBody BusinessTripProcessRequest request) {
        businessTripService.processTrip(request);
        String message = request.isApprove() ? "승인 처리되었습니다." : "반려 처리되었습니다.";
        return ResponseEntity.ok(message);
    }
}
