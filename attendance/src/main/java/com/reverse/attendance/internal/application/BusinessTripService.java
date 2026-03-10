package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.domain.BusinessTrip;
import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import com.reverse.attendance.internal.dto.request.BusinessTripApplyRequest;
import com.reverse.attendance.internal.dto.request.BusinessTripProcessRequest;
import com.reverse.attendance.internal.persistence.BusinessTripMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessTripService {

    private final BusinessTripMapper businessTripMapper;

    // 외근/출장 신청
    @Transactional
    public void applyBusinessTrip(BusinessTripApplyRequest request, Long employeeId) {
        if (request.getStartDatetime() == null || request.getEndDatetime() == null) {
            throw new IllegalArgumentException("시작/종료 일시는 필수입니다.");
        }
        if (request.getStartDatetime().isAfter(request.getEndDatetime())) {
            throw new IllegalArgumentException("종료 일시가 시작 일시보다 빠를 수 없습니다.");
        }

        // 동시성(중복 신청) 방지를 위해 직원 기준으로 DB 락 획득
        businessTripMapper.lockEmployee(employeeId);

        int overlapCount =
                businessTripMapper.countOverlappingTrips(
                        employeeId, request.getStartDatetime(), request.getEndDatetime());
        if (overlapCount > 0) {
            throw new com.reverse.core.exception.BadRequestException(
                    "해당 기간에 이미 신청했거나 승인된 외근/출장이 존재합니다.");
        }

        BusinessTrip trip =
                BusinessTrip.builder()
                        .employeeId(employeeId)
                        .tripType(request.getTripType())
                        .destination(request.getDestination())
                        .startDatetime(request.getStartDatetime())
                        .endDatetime(request.getEndDatetime())
                        .reason(request.getReason())
                        .approvalStatus(ApprovalStatus.PENDING)
                        .build();

        businessTripMapper.insertBusinessTrip(trip);
    }

    // 내 신청 내역 조회
    @Transactional(readOnly = true)
    public com.reverse.core.response.PageResponse<BusinessTrip> getMyTrips(
            Long employeeId, int page, int size) {
        page = Math.max(1, page);
        size = Math.min(100, Math.max(1, size));
        int limit = size;
        long offsetLong = (long) (page - 1) * size;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new com.reverse.core.exception.BadRequestException("조회 가능한 페이지 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;
        List<BusinessTrip> content = businessTripMapper.findByEmployeeId(employeeId, limit, offset);
        long totalElements = businessTripMapper.countByEmployeeId(employeeId);
        return com.reverse.core.response.PageResponse.of(content, page, size, totalElements);
    }

    @Transactional(readOnly = true)
    public com.reverse.attendance.internal.dto.response.RequestStatusCountResponse
            getMyRequestStatusCounts(Long employeeId) {
        return businessTripMapper.countRequestStatus(employeeId);
    }

    // 신청 취소 (대기 상태만)
    @Transactional
    public void cancelTrip(Long tripId, Long employeeId) {
        BusinessTrip trip =
                businessTripMapper
                        .findById(tripId)
                        .orElseThrow(() -> new IllegalArgumentException("해당 신청 내역을 찾을 수 없습니다."));

        if (!trip.getEmployeeId().equals(employeeId)) {
            throw new com.reverse.core.exception.ForbiddenException("본인의 신청 건만 취소할 수 있습니다.");
        }
        if (trip.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new com.reverse.core.exception.BadRequestException("결재 대기 상태인 건만 즉시 취소할 수 있습니다.");
        }

        BusinessTrip canceledTrip =
                BusinessTrip.builder()
                        .tripId(trip.getTripId())
                        .approvalStatus(ApprovalStatus.CANCELED)
                        .build();

        int updatedRows = businessTripMapper.updateStatusIfPending(canceledTrip);
        if (updatedRows == 0) {
            throw new com.reverse.core.exception.BadRequestException("이미 처리된 신청 건입니다.");
        }
    }

    // 팀원 전체 내역 조회 (관리자용)
    @Transactional(readOnly = true)
    public com.reverse.core.response.PageResponse<BusinessTrip> getAllTrips(
            String status, int page, int size) {
        if (status != null && !status.trim().isEmpty()) {
            try {
                ApprovalStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new com.reverse.core.exception.BadRequestException("유효하지 않은 결재 상태입니다.");
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
        List<BusinessTrip> content = businessTripMapper.findAll(status, limit, offset);
        long totalElements = businessTripMapper.countAll(status);
        return com.reverse.core.response.PageResponse.of(content, page, size, totalElements);
    }

    // 결재 처리 (관리자)
    @Transactional
    public void processTrip(BusinessTripProcessRequest request) {
        BusinessTrip trip =
                businessTripMapper
                        .findById(request.getTripId())
                        .orElseThrow(() -> new IllegalArgumentException("결재할 신청 내역을 찾을 수 없습니다."));

        if (trip.getApprovalStatus() != ApprovalStatus.PENDING) {
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

        BusinessTrip processedTrip =
                BusinessTrip.builder()
                        .tripId(trip.getTripId())
                        .approvalStatus(newStatus)
                        .rejectReason(rejectReason)
                        .build();

        int updatedRows = businessTripMapper.updateStatusIfPending(processedTrip);
        if (updatedRows == 0) {
            throw new com.reverse.core.exception.BadRequestException("이미 처리된 신청 건입니다.");
        }
    }
}
