package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.WeeklyWorkSchedule;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WeeklyWorkScheduleMapper {

    void insertSchedule(WeeklyWorkSchedule schedule);

    Optional<WeeklyWorkSchedule> findById(@Param("weeklyId") Long weeklyId);

    List<WeeklyWorkSchedule> findByEmployeeId(@Param("employeeId") Long employeeId);

    List<WeeklyWorkSchedule> findAll(@Param("status") String status);

    int updateStatusIfPending(WeeklyWorkSchedule schedule);
}
