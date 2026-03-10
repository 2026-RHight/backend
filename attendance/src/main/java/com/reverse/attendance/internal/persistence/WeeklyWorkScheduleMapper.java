package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.WeeklyWorkSchedule;
import com.reverse.attendance.internal.dto.response.RequestStatusCountResponse;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WeeklyWorkScheduleMapper {

    void insertSchedule(WeeklyWorkSchedule schedule);

    // 동시성 제어를 위한 직원 락
    void lockEmployee(@Param("employeeId") Long employeeId);

    Optional<WeeklyWorkSchedule> findById(@Param("weeklyId") Long weeklyId);

    List<WeeklyWorkSchedule> findByEmployeeId(
            @Param("employeeId") Long employeeId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countByEmployeeId(@Param("employeeId") Long employeeId);

    List<WeeklyWorkSchedule> findAll(
            @Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);

    long countAll(@Param("status") String status);

    int updateStatusIfPending(WeeklyWorkSchedule schedule);

    int countOverlappingSchedules(
            @Param("employeeId") Long employeeId,
            @Param("planDate") java.time.LocalDate planDate,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    RequestStatusCountResponse countRequestStatus(@Param("employeeId") Long employeeId);
}
