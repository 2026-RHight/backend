package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.Attendance;
import com.reverse.attendance.internal.dto.response.AttendanceSummaryResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AttendanceMapper {

    void insertCheckIn(Attendance attendance);

    Optional<Attendance> findByEmployeeIdAndWorkDate(
            @Param("employeeId") Long employeeId, @Param("workDate") LocalDate workDate);

    int updateCheckOut(Attendance attendance);

    void updateAttendanceByAdmin(Attendance attendance);

    // 특정 월의 상태별 통계 조회
    AttendanceSummaryResponse countMonthlySummary(
            @Param("employeeId") Long employeeId, @Param("targetMonth") String targetMonth);

    // 특정 월의 근태 기록 리스트 조회
    List<Attendance> findMonthlyRecords(
            @Param("employeeId") Long employeeId,
            @Param("targetMonth") String targetMonth,
            @Param("status") String status);

    // 급여 정산용 근태 통계 조회
    com.reverse.attendance.dto.response.PayrollAttendanceResponse getAttendanceForPayroll(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month);

    // 퇴근 미처리자 자정 마감 처리
    int autoCloseMissingCheckOut(@Param("workDate") LocalDate workDate);
}
