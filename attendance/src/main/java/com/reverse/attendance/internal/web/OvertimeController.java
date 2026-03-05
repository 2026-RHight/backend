package com.reverse.attendance.internal.web;

import com.reverse.attendance.internal.application.OvertimeService;
import com.reverse.attendance.internal.dto.request.OvertimeProcessRequest;
import com.reverse.attendance.internal.domain.Overtime;
import com.reverse.attendance.internal.dto.request.OvertimeApplyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.reverse.core.security.CustomUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/v1/overtimes")
@RequiredArgsConstructor
public class OvertimeController {

    private final OvertimeService overtimeService;

    // 사용자 : 내 신청 내역 조회
    @GetMapping("/my-requests")
    public ResponseEntity<List<Overtime>> getMyOvertimes(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(overtimeService.getMyOvertimes(user.getEmployeeId()));
    }

    // 사용자 : 연장근무 신청
    @PostMapping("/apply")
    public ResponseEntity<String> applyOvertime(@RequestBody OvertimeApplyRequest request,
            @AuthenticationPrincipal CustomUser user) {
        try {
            overtimeService.applyOvertime(request, user.getEmployeeId());
            return ResponseEntity.ok("연장근무 신청이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("신청 중 오류가 발생했습니다.");
        }
    }

    // 사용자 : 신청 취소
    @PutMapping("/{overtimeId}/cancel")
    public ResponseEntity<String> cancelOvertime(
            @PathVariable Long overtimeId,
            @AuthenticationPrincipal CustomUser user) {
        try {
            overtimeService.cancelOvertime(overtimeId, user.getEmployeeId());
            return ResponseEntity.ok("신청이 취소되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("취소 처리 중 오류가 발생했습니다.");
        }
    }

    // 관리자 : 전체 리스트 조회
    @GetMapping("/admin/requests")
    public ResponseEntity<List<Overtime>> getAllOvertimes(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(overtimeService.getAllOvertimes(status));
    }

    // 관리자 : 승인 / 반려 결재
    @PutMapping("/admin/process")
    public ResponseEntity<String> processOvertime(@RequestBody OvertimeProcessRequest request) {
        try {
            overtimeService.processOvertime(request);
            String message;
            if (request.isApprove()) {
                message = "승인 처리되었습니다.";
            } else {
                message = "반려 처리되었습니다.";
            }
            return ResponseEntity.ok(message);

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("결재 처리 중 오류가 발생했습니다.");
        }
    }
}