package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.domain.LeaveRequest;
import com.reverse.attendance.internal.domain.enums.LeaveStatus;
import com.reverse.attendance.internal.dto.request.LeaveApplyRequest;
import com.reverse.attendance.internal.dto.request.LeaveProcessRequest;
import com.reverse.attendance.internal.dto.response.LeaveBalanceResponse;
import com.reverse.attendance.internal.persistence.LeaveMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveMapper leaveMapper;
    private final com.reverse.attendance.internal.persistence.AttendanceMapper attendanceMapper;

    // 연차 현황 조회
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getLeaveBalance(Long employeeId) {
        int currentYear = java.time.LocalDate.now().getYear();
        double total =
                leaveMapper.findTotalAnnualLeaveByEmployeeId(employeeId, currentYear).orElse(0.0);

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
        int currentYear =
                request.getStartDate() != null
                        ? request.getStartDate().getYear()
                        : java.time.LocalDate.now().getYear();
        // 직원별 연차 신청 직렬화를 위한 행 잠금
        leaveMapper.lockVacationBalanceByEmployeeId(employeeId, currentYear);

        if (request.getStartDate() == null
                || request.getEndDate() == null
                || request.getLeaveType() == null) {
            throw new IllegalArgumentException("휴가 유형/시작일/종료일은 필수입니다.");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("종료일이 시작일보다 빠를 수 없습니다.");
        }

        // 중복 휴가(겹치는 기간) 검증
        int overlapCount =
                leaveMapper.countOverlappingLeaves(
                        employeeId, request.getStartDate(), request.getEndDate());
        if (overlapCount > 0) {
            throw new com.reverse.core.exception.BadRequestException(
                    "해당 기간에 이미 신청했거나 승인된 휴가가 존재합니다.");
        }

        // 차감 일수 계산 (연차면 일수 계산, 반차면 무조건 0.5일)
        double deductionDays = request.getLeaveType().getDeductionDays();

        long daysBetween = 0;
        java.time.LocalDate date = request.getStartDate();
        while (!date.isAfter(request.getEndDate())) {
            java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek != java.time.DayOfWeek.SATURDAY
                    && dayOfWeek != java.time.DayOfWeek.SUNDAY) {
                daysBetween++;
            }
            date = date.plusDays(1);
        }

        if (request.getLeaveType()
                == com.reverse.attendance.internal.domain.enums.LeaveType.ANNUAL) {
            deductionDays = daysBetween * 1.0;
        } else {
            if (!request.getStartDate().isEqual(request.getEndDate()) || daysBetween != 1) {
                throw new IllegalArgumentException("반차는 근무일 하루에만 신청할 수 있습니다.");
            }
        }

        if (deductionDays <= 0
                && request.getLeaveType()
                        == com.reverse.attendance.internal.domain.enums.LeaveType.ANNUAL) {
            throw new IllegalArgumentException("근무일이 포함된 연차만 신청할 수 있습니다.");
        }

        // 잔여 연차 검증
        LeaveBalanceResponse balance = getLeaveBalance(employeeId);
        if (balance.getRemainingAnnualLeave() < deductionDays) {
            throw new com.reverse.core.exception.BadRequestException("잔여 연차가 부족하여 휴가를 신청할 수 없습니다.");
        }

        LeaveRequest leaveRequest =
                LeaveRequest.builder()
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
    public com.reverse.core.response.PageResponse<LeaveRequest> getMyLeaveRequests(
            Long employeeId, int page, int size) {
        page = Math.max(1, page);
        size = Math.min(100, Math.max(1, size));
        int limit = size;
        int offset = (page - 1) * size;
        List<LeaveRequest> content =
                leaveMapper.findLeaveRequestsByEmployeeId(employeeId, limit, offset);
        long totalElements = leaveMapper.countByEmployeeId(employeeId);
        return com.reverse.core.response.PageResponse.of(content, page, size, totalElements);
    }

    // 휴가 신청 내역 상태별 집계
    @Transactional(readOnly = true)
    public com.reverse.attendance.internal.dto.response.RequestStatusCountResponse
            getMyRequestStatusCounts(Long employeeId) {
        return leaveMapper.countRequestStatus(employeeId);
    }

    // 휴가 취소 (대기 상태일 때만 가능)
    @Transactional
    public void cancelLeave(Long leaveRequestId, Long employeeId) {
        LeaveRequest request =
                leaveMapper
                        .findLeaveRequestById(leaveRequestId)
                        .orElseThrow(() -> new IllegalArgumentException("해당 휴가 내역을 찾을 수 없습니다."));

        if (!request.getEmployeeId().equals(employeeId)) {
            throw new com.reverse.core.exception.BadRequestException("본인의 휴가만 취소할 수 있습니다.");
        }

        if (request.getLeaveStatus() != LeaveStatus.PENDING) {
            throw new com.reverse.core.exception.BadRequestException(
                    "결재 대기 상태인 휴가만 즉시 취소할 수 있습니다.");
        }

        LeaveRequest canceledRequest =
                LeaveRequest.builder()
                        .leaveRequestId(request.getLeaveRequestId())
                        .leaveStatus(LeaveStatus.CANCELED) // 취소로 상태 변경
                        .build();

        int updatedRows = leaveMapper.updateStatusIfPending(canceledRequest);
        if (updatedRows == 0) {
            throw new com.reverse.core.exception.BadRequestException("이미 처리된 신청 건입니다.");
        }
    }

    @Transactional(readOnly = true)
    public com.reverse.core.response.PageResponse<LeaveRequest> getAllTeamLeaveRequests(
            String status, int page, int size) {
        page = Math.max(1, page);
        size = Math.min(100, Math.max(1, size));
        int limit = size;
        int offset = (page - 1) * size;
        List<LeaveRequest> content = leaveMapper.findAllLeaveRequests(status, limit, offset);
        long totalElements = leaveMapper.countAll(status);
        return com.reverse.core.response.PageResponse.of(content, page, size, totalElements);
    }

    // 관리자용 휴가 승인/반려
    @Transactional
    public void processLeaveRequest(LeaveProcessRequest request) {
        LeaveRequest leaveRequest =
                leaveMapper
                        .findLeaveRequestById(request.getLeaveRequestId())
                        .orElseThrow(() -> new IllegalArgumentException("결재할 휴가 내역을 찾을 수 없습니다."));

        if (leaveRequest.getLeaveStatus() != LeaveStatus.PENDING) {
            throw new com.reverse.core.exception.BadRequestException("대기 상태인 휴가 신청 건만 결재할 수 있습니다.");
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
        LeaveRequest processedRequest =
                LeaveRequest.builder()
                        .leaveRequestId(leaveRequest.getLeaveRequestId())
                        .leaveStatus(newStatus)
                        .rejectReason(rejectReason)
                        .build();

        int updatedRows = leaveMapper.updateStatusIfPending(processedRequest);
        if (updatedRows == 0) {
            throw new com.reverse.core.exception.BadRequestException("이미 처리된 신청 건입니다.");
        }
        // 휴가 승인 시, AttendanceService의 기능을 활용해 자동 기록 생성
        if (request.isApprove()) {
            com.reverse.attendance.internal.domain.enums.AttendanceStatus statusToSet =
                    (leaveRequest.getLeaveType()
                                    == com.reverse.attendance.internal.domain.enums.LeaveType
                                            .ANNUAL)
                            ? com.reverse.attendance.internal.domain.enums.AttendanceStatus.VACATION
                            : com.reverse.attendance.internal.domain.enums.AttendanceStatus
                                    .HALF_VACATION;

            java.time.LocalDate ptr = leaveRequest.getStartDate();
            while (!ptr.isAfter(leaveRequest.getEndDate())) {
                java.time.DayOfWeek dayOfWeek = ptr.getDayOfWeek();
                if (dayOfWeek != java.time.DayOfWeek.SATURDAY
                        && dayOfWeek != java.time.DayOfWeek.SUNDAY) {

                    // 해당 일자의 근태 기록이 이미 있다면 업데이트, 없다면 새로 INSERT
                    java.util.Optional<com.reverse.attendance.internal.domain.Attendance>
                            existingRecord =
                                    attendanceMapper.findByEmployeeIdAndWorkDate(
                                            leaveRequest.getEmployeeId(), ptr);

                    if (existingRecord.isPresent()) {
                        com.reverse.attendance.internal.domain.Attendance rec =
                                existingRecord.get();
                        com.reverse.attendance.internal.domain.Attendance updatedRec =
                                com.reverse.attendance.internal.domain.Attendance.builder()
                                        .attendanceId(rec.getAttendanceId())
                                        .employeeId(rec.getEmployeeId())
                                        .workDate(rec.getWorkDate())
                                        .checkInTime(rec.getCheckInTime())
                                        .checkOutTime(rec.getCheckOutTime())
                                        .status(statusToSet)
                                        .modifyReason("휴가 승인으로 인한 자동 변경")
                                        .build();
                        attendanceMapper.updateAttendanceByAdmin(updatedRec);
                    } else {
                        com.reverse.attendance.internal.domain.Attendance newRec =
                                com.reverse.attendance.internal.domain.Attendance.builder()
                                        .employeeId(leaveRequest.getEmployeeId())
                                        .workDate(ptr)
                                        .status(statusToSet)
                                        .build();
                        attendanceMapper.insertCheckIn(newRec);
                    }
                }
                ptr = ptr.plusDays(1);
            }
        }
    }
}
