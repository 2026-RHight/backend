package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.dto.request.LeaveApplyRequest;
import com.reverse.attendance.internal.dto.request.LeaveProcessRequest;
import com.reverse.attendance.internal.dto.response.LeaveBalanceResponse;
import com.reverse.attendance.internal.domain.LeaveRequest;
import com.reverse.attendance.internal.domain.enums.LeaveStatus;
import com.reverse.attendance.internal.persistence.LeaveMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveMapper leaveMapper;
    private final com.reverse.attendance.internal.persistence.AttendanceMapper attendanceMapper;

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
    public void applyLeave(LeaveApplyRequest request, Long employeeId) {
        // 차감 일수 계산 (연차면 일수 계산, 반차면 무조건 0.5일)
        double deductionDays = request.getLeaveType().getDeductionDays();
        if (request.getLeaveType() == com.reverse.attendance.internal.domain.enums.LeaveType.ANNUAL) {
            long daysBetween = 0;
            java.time.LocalDate date = request.getStartDate();
            while (!date.isAfter(request.getEndDate())) {
                java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
                if (dayOfWeek != java.time.DayOfWeek.SATURDAY && dayOfWeek != java.time.DayOfWeek.SUNDAY) {
                    daysBetween++;
                }
                date = date.plusDays(1);
            }
            deductionDays = daysBetween * 1.0;
        }

        // 잔여 연차 검증
        LeaveBalanceResponse balance = getLeaveBalance(employeeId);
        if (balance.getRemainingAnnualLeave() < deductionDays) {
            throw new IllegalStateException("잔여 연차가 부족하여 휴가를 신청할 수 없습니다.");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employeeId(employeeId)
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
        // 휴가 승인 시, AttendanceService의 기능을 활용해 자동 기록 생성
        if (request.isApprove()) {
            java.time.LocalDate ptr = leaveRequest.getStartDate();
            while (!ptr.isAfter(leaveRequest.getEndDate())) {
                java.time.DayOfWeek dayOfWeek = ptr.getDayOfWeek();
                if (dayOfWeek != java.time.DayOfWeek.SATURDAY && dayOfWeek != java.time.DayOfWeek.SUNDAY) {

                    // 해당 일자의 근태 기록이 이미 있다면 업데이트, 없다면 새로 INSERT
                    java.util.Optional<com.reverse.attendance.internal.domain.Attendance> existingRecord = attendanceMapper
                            .findByEmployeeIdAndWorkDate(leaveRequest.getEmployeeId(), ptr);

                    if (existingRecord.isPresent()) {
                        com.reverse.attendance.internal.domain.Attendance rec = existingRecord.get();
                        com.reverse.attendance.internal.domain.Attendance updatedRec = com.reverse.attendance.internal.domain.Attendance
                                .builder()
                                .attendanceId(rec.getAttendanceId())
                                .employeeId(rec.getEmployeeId())
                                .workDate(rec.getWorkDate())
                                .checkInTime(rec.getCheckInTime())
                                .checkOutTime(rec.getCheckOutTime())
                                .status(com.reverse.attendance.internal.domain.enums.AttendanceStatus.VACATION)
                                .modifyReason("휴가 승인으로 인한 자동 변경")
                                .build();
                        attendanceMapper.updateAttendanceByAdmin(updatedRec);
                    } else {
                        com.reverse.attendance.internal.domain.Attendance newRec = com.reverse.attendance.internal.domain.Attendance
                                .builder()
                                .employeeId(leaveRequest.getEmployeeId())
                                .workDate(ptr)
                                .status(com.reverse.attendance.internal.domain.enums.AttendanceStatus.VACATION)
                                .build();
                        attendanceMapper.insertCheckIn(newRec);
                    }
                }
                ptr = ptr.plusDays(1);
            }
        }
    }
}