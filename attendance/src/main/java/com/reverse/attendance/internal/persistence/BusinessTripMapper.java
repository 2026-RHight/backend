package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.BusinessTrip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BusinessTripMapper {

    // 신청
    void insertBusinessTrip(BusinessTrip businessTrip);

    // 내 신청 내역 조회
    List<BusinessTrip> findByEmployeeId(@Param("employeeId") Long employeeId);

    // 단건 조회 (결재 및 취소용)
    Optional<BusinessTrip> findById(@Param("tripId") Long tripId);

    // 상태 업데이트 (승인/반려/취소)
    void updateStatus(BusinessTrip businessTrip);

    // 전체 내역 조회(관리자용)
    List<BusinessTrip> findAll(@Param("approvalStatus") String approvalStatus);
}