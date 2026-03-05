package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.BusinessTripService;
import com.reverse.attendance.internal.dto.request.BusinessTripApplyRequest;
import com.reverse.attendance.internal.dto.request.BusinessTripProcessRequest;
import com.reverse.attendance.internal.domain.BusinessTrip;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.reverse.core.security.CustomUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/business-trips")
@RequiredArgsConstructor
public class BusinessTripController {

    private final BusinessTripService businessTripService;

    // 사용자 : 내 신청 내역 조회
    @GetMapping("/my-requests")
    public ResponseEntity<List<BusinessTrip>> getMyTrips(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(businessTripService.getMyTrips(user.getEmployeeId()));
    }

    // 사용자 : 외근/출장 신청
    @PostMapping("/apply")
    public ResponseEntity<String> applyTrip(@Valid @RequestBody BusinessTripApplyRequest request,
            @AuthenticationPrincipal CustomUser user) {
        try {
            businessTripService.applyBusinessTrip(request, user.getEmployeeId());
            return ResponseEntity.ok("외근/출장 신청이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("신청 중 오류가 발생했습니다.");
        }
    }

    // 사용자 : 신청 취소
    @PutMapping("/{tripId}/cancel")
    public ResponseEntity<String> cancelTrip(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUser user) {
        try {
            businessTripService.cancelTrip(tripId, user.getEmployeeId());
            return ResponseEntity.ok("신청이 취소되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("취소 처리 중 오류가 발생했습니다.");
        }
    }

    // 관리자 : 전체 리스트 조회
    @GetMapping("/admin/requests")
    public ResponseEntity<List<BusinessTrip>> getAllTrips(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(businessTripService.getAllTrips(status));
    }

    // 관리자 : 승인/반려 결재
    @PutMapping("/admin/process")
    public ResponseEntity<String> processTrip(@RequestBody BusinessTripProcessRequest request) {
        try {
            businessTripService.processTrip(request);
            String message = request.isApprove() ? "승인 처리되었습니다." : "반려 처리되었습니다.";
            return ResponseEntity.ok(message);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("결재 처리 중 오류가 발생했습니다.");
        }
    }
}