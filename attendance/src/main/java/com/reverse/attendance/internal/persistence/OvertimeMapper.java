package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.Overtime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface OvertimeMapper {

    void insertOvertime(Overtime overtime);

    List<Overtime> findByEmployeeId(@Param("employeeId") Long employeeId);

    Optional<Overtime> findById(@Param("overtimeId") Long overtimeId);

    int updateStatusIfPending(Overtime overtime);

    List<Overtime> findAll(@Param("approvalStatus") String approvalStatus);

}
