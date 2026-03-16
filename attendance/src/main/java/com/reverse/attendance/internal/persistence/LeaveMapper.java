package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.LeaveGrantHistory;
import com.reverse.attendance.internal.domain.LeaveRequest;
import com.reverse.attendance.internal.dto.response.RequestStatusCountResponse;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LeaveMapper {

    Optional<Double> findTotalAnnualLeaveByEmployeeId(
            @Param("employeeId") Long employeeId, @Param("baseYear") int baseYear);

    List<LeaveGrantHistory> findLeaveGrantHistoryByEmployeeId(
            @Param("employeeId") Long employeeId, @Param("baseYear") Integer baseYear);

    void lockVacationBalanceByEmployeeId(
            @Param("employeeId") Long employeeId, @Param("baseYear") int baseYear);

    Double sumUsedDaysByStatus(
            @Param("employeeId") Long employeeId,
            @Param("status") String status,
            @Param("year") int year);

    Double sumLegacyUsedDaysByStatus(
            @Param("employeeId") Long employeeId,
            @Param("status") String status,
            @Param("year") int year);

    void insertLeaveRequest(LeaveRequest leaveRequest);

    List<LeaveRequest> findLeaveRequestsByEmployeeId(
            @Param("employeeId") Long employeeId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    List<LeaveRequest> findLeaveRequestsByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    List<LeaveRequest> findTeamLeaveRequestsByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    long countByEmployeeId(@Param("employeeId") Long employeeId);

    Optional<LeaveRequest> findLeaveRequestById(@Param("leaveRequestId") Long leaveRequestId);

    int updateStatusIfPending(LeaveRequest leaveRequest);

    List<LeaveRequest> findTeamLeaveRequests(
            @Param("actorEmployeeId") Long actorEmployeeId,
            @Param("leaveStatus") String leaveStatus,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countTeamLeaveRequests(
            @Param("actorEmployeeId") Long actorEmployeeId,
            @Param("leaveStatus") String leaveStatus);

    boolean isSameTeamLeaveRequest(
            @Param("actorEmployeeId") Long actorEmployeeId,
            @Param("leaveRequestId") Long leaveRequestId);

    long countAll(@Param("leaveStatus") String leaveStatus);

    Optional<com.reverse.attendance.internal.domain.enums.LeaveType> findApprovedLeaveTypeByDate(
            @Param("employeeId") Long employeeId, @Param("date") java.time.LocalDate date);

    int countOverlappingLeaves(
            @Param("employeeId") Long employeeId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    RequestStatusCountResponse countRequestStatus(@Param("employeeId") Long employeeId);

    // 자정 배치 작업용 (신년 연차 일괄 부여)
    int insertNextYearLeaveBalance(@Param("baseYear") int baseYear);
}
