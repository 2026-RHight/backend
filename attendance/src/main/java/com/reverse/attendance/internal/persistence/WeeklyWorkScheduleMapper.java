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

    Optional<WeeklyWorkSchedule> findApprovedByEmployeeIdAndPlanDate(
            @Param("employeeId") Long employeeId, @Param("planDate") java.time.LocalDate planDate);

    List<WeeklyWorkSchedule> findByEmployeeId(
            @Param("employeeId") Long employeeId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    List<WeeklyWorkSchedule> findByEmployeeIdAndPlanDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    long countByEmployeeId(@Param("employeeId") Long employeeId);

    List<WeeklyWorkSchedule> findAll(
            @Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);

    List<WeeklyWorkSchedule> findTeamSchedules(
            @Param("actorEmployeeId") Long actorEmployeeId,
            @Param("status") String status,
            @Param("limit") int limit,
            @Param("offset") int offset);

    List<WeeklyWorkSchedule> findTeamSchedulesByPlanDateRange(
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    List<WeeklyWorkSchedule> findTeamSchedulesByEmployeeIdAndPlanDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    long countAll(@Param("status") String status);

    long countTeamSchedules(
            @Param("actorEmployeeId") Long actorEmployeeId, @Param("status") String status);

    boolean isSameTeamSchedule(
            @Param("actorEmployeeId") Long actorEmployeeId, @Param("weeklyId") Long weeklyId);

    int updateStatusIfPending(WeeklyWorkSchedule schedule);

    int countOverlappingSchedules(
            @Param("employeeId") Long employeeId,
            @Param("planDate") java.time.LocalDate planDate,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    RequestStatusCountResponse countRequestStatus(@Param("employeeId") Long employeeId);
}
