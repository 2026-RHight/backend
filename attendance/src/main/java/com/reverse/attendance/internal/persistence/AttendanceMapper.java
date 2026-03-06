package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.dto.response.AttendanceSummaryResponse;
import com.reverse.attendance.internal.domain.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AttendanceMapper {

        void insertCheckIn(Attendance attendance);

        Optional<Attendance> findByEmployeeIdAndWorkDate(
                        @Param("employeeId") Long employeeId,
                        @Param("workDate") LocalDate workDate);

        int updateCheckOut(Attendance attendance);

        void updateAttendanceByAdmin(Attendance attendance);

        // 특정 월의 상태별 통계 조회
        AttendanceSummaryResponse countMonthlySummary(
                        @Param("employeeId") Long employeeId,
                        @Param("yearMonth") String yearMonth);

        // 특정 월의 근태 기록 리스트 조회
        List<Attendance> findMonthlyRecords(
                        @Param("employeeId") Long employeeId,
                        @Param("yearMonth") String yearMonth,
                        @Param("status") String status);

}
