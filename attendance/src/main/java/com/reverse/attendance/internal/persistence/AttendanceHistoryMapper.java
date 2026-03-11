package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.AttendanceHistory;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AttendanceHistoryMapper {

    void insertHistory(AttendanceHistory history);

    List<AttendanceHistory> findHistory(
            @Param("employeeId") Long employeeId,
            @Param("targetMonth") String targetMonth,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countHistory(
            @Param("employeeId") Long employeeId, @Param("targetMonth") String targetMonth);
}
