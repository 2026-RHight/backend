package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Optional;

@Mapper
public interface AttendanceMapper {

    void insertCheckIn(Attendance attendance);

    Optional<Attendance> findByEmployeeIdAndWorkDate(
            @Param("employeeId") Long employeeId,
            @Param("workDate")LocalDate workDate
    );

    void updateCheckOut(Attendance attendance);

    void updateAttendanceByAdmin(Attendance attendance);

}
