package com.reverse.attendance.internal.persistence;

import com.reverse.attendance.internal.domain.Attendance;
import com.reverse.attendance.internal.dto.response.AdminAttendanceReportResponse;
import com.reverse.attendance.internal.dto.response.AttendanceSummaryResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AttendanceMapper {

    void insertCheckIn(Attendance attendance);

    int insertOrUpdateAttendance(Attendance attendance);

    Optional<Attendance> findByEmployeeIdAndWorkDate(
            @Param("employeeId") Long employeeId, @Param("workDate") LocalDate workDate);

    List<Attendance> findAttendancesByMonth(@Param("targetMonth") String targetMonth);

    List<Attendance> findOpenAttendancesByDate(@Param("workDate") LocalDate workDate);

    List<Attendance> findOpenAttendancesByMonth(@Param("targetMonth") String targetMonth);

    int updateCheckOut(Attendance attendance);

    int updateAttendanceByAdmin(Attendance attendance);

    // 특정 월의 상태별 통계 조회
    AttendanceSummaryResponse countMonthlySummary(
            @Param("employeeId") Long employeeId, @Param("targetMonth") String targetMonth);

    AttendanceSummaryResponse countCompanyMonthlySummary(@Param("targetMonth") String targetMonth);

    // 특정 월의 근태 기록 리스트 조회
    List<Attendance> findMonthlyRecords(
            @Param("employeeId") Long employeeId,
            @Param("targetMonth") String targetMonth,
            @Param("status") String status);

    List<AdminAttendanceReportResponse> findMonthlyEmployeeReports(
            @Param("targetMonth") String targetMonth,
            @Param("baseYear") int baseYear,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countMonthlyEmployeeReports(@Param("targetMonth") String targetMonth);

    // 급여 정산용 근태 통계 조회
    com.reverse.attendance.dto.response.PayrollAttendanceResponse getAttendanceForPayroll(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month);

    // 퇴근 미처리자 자정 마감 처리
    int autoCloseMissingCheckOut(@Param("workDate") LocalDate workDate);

    int autoCloseMissingCheckOutByMonth(@Param("targetMonth") String targetMonth);

    int closeMonthlyRecords(@Param("targetMonth") String targetMonth);

    int reopenMonthlyRecords(@Param("targetMonth") String targetMonth);

    int updateOvertimeHours(
            @Param("employeeId") Long employeeId,
            @Param("workDate") LocalDate workDate,
            @Param("overtimeHours") BigDecimal overtimeHours,
            @Param("modifyReason") String modifyReason);
}
