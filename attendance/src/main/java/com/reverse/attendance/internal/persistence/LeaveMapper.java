package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.LeaveRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface LeaveMapper {

    Optional<Double> findTotalAnnualLeaveByEmployeeId(@Param("employeeId") Long employeeId);

    Double sumUsedDaysByStatus(@Param("employeeId") Long employeeId, @Param("status") String status);

    void insertLeaveRequest(LeaveRequest leaveRequest);

    List<LeaveRequest> findLeaveRequestsByEmployeeId(@Param("employeeId") Long employeeId);

    Optional<LeaveRequest> findLeaveRequestById(@Param("leaveRequestId") Long leaveRequestId);

    int updateStatusIfPending(LeaveRequest leaveRequest);

    List<LeaveRequest> findAllLeaveRequests(@Param("leaveStatus") String leaveStatus);

}