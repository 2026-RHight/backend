package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.AttendancePolicy;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AttendancePolicyMapper {
    // 특정 사원의 현재 적용 중인 근태 규정 조회
    Optional<AttendancePolicy> findByEmployeeId(@Param("employeeId") Long employeeId);
}
