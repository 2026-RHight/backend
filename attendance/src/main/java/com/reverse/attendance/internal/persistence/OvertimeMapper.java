package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.Overtime;
import com.reverse.attendance.internal.dto.response.RequestStatusCountResponse;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OvertimeMapper {

    void insertOvertime(Overtime overtime);

    List<Overtime> findByEmployeeId(
            @Param("employeeId") Long employeeId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countByEmployeeId(@Param("employeeId") Long employeeId);

    Optional<Overtime> findById(@Param("overtimeId") Long overtimeId);

    int updateStatusIfPending(Overtime overtime);

    List<Overtime> findAll(
            @Param("approvalStatus") String approvalStatus,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countAll(@Param("approvalStatus") String approvalStatus);

    int countOverlappingOvertimes(
            @Param("employeeId") Long employeeId,
            @Param("workDate") java.time.LocalDate workDate,
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime);

    RequestStatusCountResponse countRequestStatus(@Param("employeeId") Long employeeId);
}
