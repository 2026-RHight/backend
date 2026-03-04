package com.reverse.attendance.internal.application;


import com.reverse.attendance.dto.request.LeaveApplyRequest;
import com.reverse.attendance.dto.request.LeaveProcessRequest;
import com.reverse.attendance.dto.response.LeaveBalanceResponse;
import com.reverse.attendance.internal.domain.LeaveRequest;
import com.reverse.attendance.internal.domain.enums.LeaveStatus;
import com.reverse.attendance.internal.persistence.LeaveMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveMapper leaveMapper;

    // 연차 현황 조회
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getLeaveBalance(Long employeeId) {
        double total = leaveMapper.findTotalAnnualLeaveByEmployeeId(employeeId)
                .orElse(0.0);

        double used = leaveMapper.sumUsedDaysByStatus(employeeId, LeaveStatus.APPROVED.name());
        double pending = leaveMapper.sumUsedDaysByStatus(employeeId, LeaveStatus.PENDING.name());
        double remaining = total - used - pending;

        return LeaveBalanceResponse.builder()
                .totalAnnualLeave(total)
                .usedAnnualLeave(used)
                .pendingAnnualLeave(pending)
                .remainingAnnualLeave(remaining)
                .build();
    }

    // 휴가 신청
    @Transactional
    public void applyLeave(LeaveApplyRequest request) {
        // 차감 일수 계산 (연차면 일수 계산, 반차면 무조건 0.5일)
        double deductionDays = request.getLeaveType().getDeductionDays();
        if (request.getLeaveType() == com.reverse.attendance.internal.domain.enums.LeaveType.ANNUAL) {
            long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
            deductionDays = daysBetween * 1.0;
        }

        // 잔여 연차 검증
        LeaveBalanceResponse balance = getLeaveBalance(request.getEmployeeId());
        if (balance.getRemainingAnnualLeave() < deductionDays) {
            throw new IllegalStateException("잔여 연차가 부족하여 휴가를 신청할 수 없습니다.");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employeeId(request.getEmployeeId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .leaveType(request.getLeaveType())
                .leaveStatus(LeaveStatus.PENDING)
                .usedDays(deductionDays)
                .reason(request.getReason())
                .build();

        leaveMapper.insertLeaveRequest(leaveRequest);
    }

    // 나의 휴가 내역 리스트 조회
    @Transactional(readOnly = true)
    public List<LeaveRequest> getMyLeaveRequests(Long employeeId) {
        return leaveMapper.findLeaveRequestsByEmployeeId(employeeId);
    }

    // 휴가 취소 (대기 상태일 때만 가능)
    @Transactional
    public void cancelLeave(Long leaveRequestId, Long employeeId) {
        LeaveRequest request = leaveMapper.findLeaveRequestById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("해당 휴가 내역을 찾을 수 없습니다."));

        if (!request.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("본인의 휴가만 취소할 수 있습니다.");
        }

        if (request.getLeaveStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("결재 대기 상태인 휴가만 즉시 취소할 수 있습니다.");
        }

        LeaveRequest canceledRequest = LeaveRequest.builder()
                .leaveRequestId(request.getLeaveRequestId())
                .leaveStatus(LeaveStatus.CANCELED) // 취소로 상태 변경
                .build();

        leaveMapper.updateLeaveStatus(canceledRequest);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getAllTeamLeaveRequests(String status) {
        return leaveMapper.findAllLeaveRequests(status);
    }

    // 관리자용 휴가 승인/반려
    @Transactional
    public void processLeaveRequest(LeaveProcessRequest request) {
        LeaveRequest leaveRequest = leaveMapper.findLeaveRequestById(request.getLeaveRequestId())
                .orElseThrow(() -> new IllegalArgumentException("결재할 휴가 내역을 찾을 수 없습니다."));

        if (leaveRequest.getLeaveStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("대기 상태인 휴가 신청 건만 결재할 수 있습니다.");
        }

        LeaveStatus newStatus;
        String rejectReason = null;

        if (request.isApprove()) {
            newStatus = LeaveStatus.APPROVED;
        } else {
            newStatus = LeaveStatus.REJECTED;
            if (request.getRejectReason() == null || request.getRejectReason().trim().isEmpty()) {
                throw new IllegalArgumentException("휴가 반려 시 팀장 코멘트(반려 사유)를 반드시 입력해야 합니다.");
            }
            rejectReason = request.getRejectReason();
        }

        // 상태 업데이트
        LeaveRequest processedRequest = LeaveRequest.builder()
                .leaveRequestId(leaveRequest.getLeaveRequestId())
                .leaveStatus(newStatus)
                .rejectReason(rejectReason)
                .build();

        leaveMapper.updateLeaveStatus(processedRequest);
    }
}