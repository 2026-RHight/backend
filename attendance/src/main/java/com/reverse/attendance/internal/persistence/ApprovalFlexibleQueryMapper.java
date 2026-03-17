package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.dto.response.WeeklyWorkScheduleResponse;
import com.reverse.attendance.internal.persistence.row.FlexibleApprovalHeaderRow;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApprovalFlexibleQueryMapper {
    List<WeeklyWorkScheduleResponse> findTeamFlexibleApprovalSchedules(
            @Param("actorEmployeeId") Long actorEmployeeId,
            @Param("status") String status,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countTeamFlexibleApprovalSchedules(
            @Param("actorEmployeeId") Long actorEmployeeId, @Param("status") String status);

    Optional<FlexibleApprovalHeaderRow> findFlexibleApprovalHeaderByApprovalId(
            @Param("approvalId") Long approvalId);
}
