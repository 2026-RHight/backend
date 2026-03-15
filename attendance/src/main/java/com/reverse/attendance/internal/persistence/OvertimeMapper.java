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

    // 동시성 이슈 방지를 위한 직원 레코드 락
    void lockEmployee(@Param("employeeId") Long employeeId);

    List<Overtime> findByEmployeeId(
            @Param("employeeId") Long employeeId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    List<Overtime> findByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    List<Overtime> findTeamOvertimesByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

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
