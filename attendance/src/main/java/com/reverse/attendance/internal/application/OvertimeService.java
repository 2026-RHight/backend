package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.domain.Overtime;
import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import com.reverse.attendance.internal.dto.request.OvertimeApplyRequest;
import com.reverse.attendance.internal.dto.request.OvertimeProcessRequest;
import com.reverse.attendance.internal.dto.response.OvertimeResponse;
import com.reverse.attendance.internal.persistence.OvertimeMapper;
import com.reverse.core.response.PageResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OvertimeService {

    private final OvertimeMapper overtimeMapper;
    private final AttendanceSyncService attendanceSyncService;

    @Transactional
    public void applyOvertime(OvertimeApplyRequest request, Long employeeId) {
        applyOvertime(request, employeeId, null);
    }

    @Transactional
    public void applyOvertime(OvertimeApplyRequest request, Long employeeId, Long approvalId) {
        if (request == null
                || request.getWorkDate() == null
                || request.getStartTime() == null
                || request.getEndTime() == null) {
            throw new IllegalArgumentException("근무 일자와 시작/종료 시간은 필수입니다.");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("연장근무 종료 시간이 시작 시간보다 빠를 수 없습니다.");
        }

        // 동시성(중복 신청) 방지를 위해 직원 기준으로 DB 락 획득
        overtimeMapper.lockEmployee(employeeId);

        int overlapCount =
                overtimeMapper.countOverlappingOvertimes(
                        employeeId,
                        request.getWorkDate(),
                        request.getStartTime(),
                        request.getEndTime());
        if (overlapCount > 0) {
            throw new com.reverse.core.exception.BadRequestException(
                    "해당 시간대에 이미 신청했거나 승인된 연장근무가 존재합니다.");
        }

        Overtime overtime =
                Overtime.builder()
                        .approvalId(approvalId)
                        .employeeId(employeeId)
                        .workDate(request.getWorkDate())
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .reason(request.getReason())
                        .approvalStatus(ApprovalStatus.PENDING)
                        .build();

        overtimeMapper.insertOvertime(overtime);
    }

    @Transactional
    public void deleteLinkedRequestByApprovalId(Long approvalId) {
        if (approvalId == null) {
            return;
        }
        overtimeMapper.deleteByApprovalId(approvalId);
    }

    @Transactional(readOnly = true)
    public PageResponse<OvertimeResponse> getMyOvertimes(Long employeeId, int page, int size) {
        page = Math.max(1, page);
        size = Math.min(100, Math.max(1, size));
        int limit = size;
        long offsetLong = (long) (page - 1) * size;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new com.reverse.core.exception.BadRequestException("조회 가능한 페이지 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;
        List<Overtime> content = overtimeMapper.findByEmployeeId(employeeId, limit, offset);
        long totalElements = overtimeMapper.countByEmployeeId(employeeId);
        return PageResponse.of(
                content.stream().map(OvertimeResponse::from).collect(Collectors.toList()),
                page,
                size,
                totalElements);
    }

    @Transactional(readOnly = true)
    public com.reverse.attendance.internal.dto.response.RequestStatusCountResponse
            getMyRequestStatusCounts(Long employeeId) {
        return overtimeMapper.countRequestStatus(employeeId);
    }

    @Transactional
    public void cancelOvertime(Long overtimeId, Long employeeId) {
        Overtime overtime =
                overtimeMapper
                        .findById(overtimeId)
                        .orElseThrow(
                                () -> new IllegalArgumentException("해당 연장근무 신청 내역을 찾을 수 없습니다."));

        if (!overtime.getEmployeeId().equals(employeeId)) {
            throw new com.reverse.core.exception.ForbiddenException("본인의 신청 건만 취소할 수 있습니다.");
        }
        if (overtime.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new com.reverse.core.exception.BadRequestException("결재 대기 상태인 건만 취소할 수 있습니다.");
        }

        Overtime canceledOvertime =
                Overtime.builder()
                        .overtimeId(overtime.getOvertimeId())
                        .approvalStatus(ApprovalStatus.CANCELED)
                        .build();

        int updatedRows = overtimeMapper.updateStatusIfPending(canceledOvertime);
        if (updatedRows == 0) {
            throw new com.reverse.core.exception.BadRequestException("이미 처리된 신청 건입니다.");
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<OvertimeResponse> getAllOvertimes(String status, int page, int size) {
        if (status != null) {
            status = status.trim();
            if (status.isEmpty()) {
                status = null;
            } else {
                try {
                    status = ApprovalStatus.valueOf(status).name();
                } catch (IllegalArgumentException e) {
                    throw new com.reverse.core.exception.BadRequestException("유효하지 않은 결재 상태입니다.");
                }
            }
        }
        page = Math.max(1, page);
        size = Math.min(100, Math.max(1, size));
        int limit = size;
        long offsetLong = (long) (page - 1) * size;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new com.reverse.core.exception.BadRequestException("조회 가능한 페이지 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;
        List<Overtime> content = overtimeMapper.findAll(status, limit, offset);
        long totalElements = overtimeMapper.countAll(status);
        return PageResponse.of(
                content.stream().map(OvertimeResponse::from).collect(Collectors.toList()),
                page,
                size,
                totalElements);
    }

    @Transactional
    public void processOvertime(OvertimeProcessRequest request) {
        Overtime overtime =
                overtimeMapper
                        .findById(request.getOvertimeId())
                        .orElseThrow(() -> new IllegalArgumentException("결재할 신청 내역을 찾을 수 없습니다."));

        if (overtime.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new com.reverse.core.exception.BadRequestException("대기 상태인 신청 건만 결재할 수 있습니다.");
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

        Overtime processedOvertime =
                Overtime.builder()
                        .overtimeId(overtime.getOvertimeId())
                        .approvalStatus(newStatus)
                        .rejectReason(rejectReason)
                        .build();

        int updatedRows = overtimeMapper.updateStatusIfPending(processedOvertime);
        if (updatedRows == 0) {
            throw new com.reverse.core.exception.BadRequestException("이미 처리된 신청 건입니다.");
        }
        if (request.isApprove()) {
            Overtime approvedOvertime =
                    overtimeMapper
                            .findById(overtime.getOvertimeId())
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "승인된 연장근무 신청 내역을 다시 조회할 수 없습니다."));
            attendanceSyncService.syncApprovedOvertime(approvedOvertime);
        }
    }
}
