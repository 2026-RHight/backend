package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.BusinessTrip;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BusinessTripMapper {

    // 신청
    void insertBusinessTrip(BusinessTrip businessTrip);

    // 동시성 제어를 위한 직원 락
    void lockEmployee(@Param("employeeId") Long employeeId);

    // 내 신청 내역 조회
    List<BusinessTrip> findByEmployeeId(
            @Param("employeeId") Long employeeId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    List<BusinessTrip> findByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    List<BusinessTrip> findTeamTripsByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    long countByEmployeeId(@Param("employeeId") Long employeeId);

    int countOverlappingTrips(
            @Param("employeeId") Long employeeId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    // 단건 조회 (결재 및 취소용)
    Optional<BusinessTrip> findById(@Param("tripId") Long tripId);

    // 상태 업데이트 (승인/반려/취소)
    int updateStatusIfPending(BusinessTrip businessTrip);

    // 전체 내역 조회(관리자용)
    List<BusinessTrip> findAll(
            @Param("approvalStatus") String approvalStatus,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countAll(@Param("approvalStatus") String approvalStatus);

    com.reverse.attendance.internal.dto.response.RequestStatusCountResponse countRequestStatus(
            @Param("employeeId") Long employeeId);
}
