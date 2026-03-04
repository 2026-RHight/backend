package com.reverse.attendance.internal.application;

import com.reverse.attendance.dto.request.OvertimeProcessRequest;
import com.reverse.attendance.internal.domain.Overtime;
import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import com.reverse.attendance.internal.persistence.OvertimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OvertimeService {

    private final OvertimeMapper overtimeMapper;

    @Transactional
    public void applyOvertime(com.reverse.attendance.dto.requestq.OvertimeApplyRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("연장근무 종료 시간이 시작 시간보다 빠를 수 없습니다.");
        }

        Overtime overtime = Overtime.builder()
                .employeeId(request.getEmployeeId())
                .workDate(request.getWorkDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason())
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        overtimeMapper.insertOvertime(overtime);
    }

    @Transactional(readOnly = true)
    public List<Overtime> getMyOvertimes(Long employeeId) {
        return overtimeMapper.findByEmployeeId(employeeId);
    }

    @Transactional
    public void cancelOvertime(Long overtimeId, Long employeeId) {
        Overtime overtime = overtimeMapper.findById(overtimeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 연장근무 신청 내역을 찾을 수 없습니다."));

        if (!overtime.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("본인의 신청 건만 취소할 수 있습니다.");
        }
        if (overtime.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("결재 대기 상태인 건만 취소할 수 있습니다.");
        }

        Overtime canceledOvertime = Overtime.builder()
                .overtimeId(overtime.getOvertimeId())
                .approvalStatus(ApprovalStatus.CANCELED)
                .build();

        overtimeMapper.updateStatus(canceledOvertime);
    }

    @Transactional(readOnly = true)
    public List<Overtime> getAllOvertimes(String status) {
        return overtimeMapper.findAll(status);
    }

    @Transactional
    public void processOvertime(OvertimeProcessRequest request) {
        Overtime overtime = overtimeMapper.findById(request.getOvertimeId())
                .orElseThrow(() -> new IllegalArgumentException("결재할 신청 내역을 찾을 수 없습니다."));

        if (overtime.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("대기 상태인 신청 건만 결재할 수 있습니다.");
        }

        ApprovalStatus newStatus;
        String rejectReason = null;

        if (request.isApprove()) {
            newStatus = ApprovalStatus.APPROVED;
        } else {
            newStatus = ApprovalStatus.REJECTED;
            if (request.getRejectReason() == null || request.getRejectReason().trim().isEmpty()) {
                throw new IllegalArgumentException("반려 시 반려 사유를 반드시 입력해야 합니다.");
            }
            rejectReason = request.getRejectReason();
        }

        Overtime processedOvertime = Overtime.builder()
                .overtimeId(overtime.getOvertimeId())
                .approvalStatus(newStatus)
                .rejectReason(rejectReason)
                .build();

        overtimeMapper.updateStatus(processedOvertime);
    }
}