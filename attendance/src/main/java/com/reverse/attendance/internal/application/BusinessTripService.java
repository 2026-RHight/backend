package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.dto.request.BusinessTripApplyRequest;
import com.reverse.attendance.internal.dto.request.BusinessTripProcessRequest;
import com.reverse.attendance.internal.domain.BusinessTrip;
import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import com.reverse.attendance.internal.persistence.BusinessTripMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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

        BusinessTrip trip = BusinessTrip.builder()
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
    public List<BusinessTrip> getMyTrips(Long employeeId) {
        return businessTripMapper.findByEmployeeId(employeeId);
    }

    // 신청 취소 (대기 상태만)
    @Transactional
    public void cancelTrip(Long tripId, Long employeeId) {
        BusinessTrip trip = businessTripMapper.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신청 내역을 찾을 수 없습니다."));

        if (!trip.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("본인의 신청 건만 취소할 수 있습니다.");
        }
        if (trip.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("결재 대기 상태인 건만 즉시 취소할 수 있습니다.");
        }

        BusinessTrip canceledTrip = BusinessTrip.builder()
                .tripId(trip.getTripId())
                .approvalStatus(ApprovalStatus.CANCELED)
                .build();

        int updatedRows = businessTripMapper.updateStatusIfPending(canceledTrip);
        if (updatedRows == 0) {
            throw new IllegalStateException("이미 처리된 신청 건입니다.");
        }
    }

    // 팀원 전체 내역 조회 (관리자용)
    @Transactional(readOnly = true)
    public List<BusinessTrip> getAllTrips(String status) {
        return businessTripMapper.findAll(status);
    }

    // 결재 처리 (관리자)
    @Transactional
    public void processTrip(BusinessTripProcessRequest request) {
        BusinessTrip trip = businessTripMapper.findById(request.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("결재할 신청 내역을 찾을 수 없습니다."));

        if (trip.getApprovalStatus() != ApprovalStatus.PENDING) {
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

        BusinessTrip processedTrip = BusinessTrip.builder()
                .tripId(trip.getTripId())
                .approvalStatus(newStatus)
                .rejectReason(rejectReason)
                .build();

        int updatedRows = businessTripMapper.updateStatusIfPending(processedTrip);
        if (updatedRows == 0) {
            throw new IllegalStateException("이미 처리된 신청 건입니다.");
        }
    }
}