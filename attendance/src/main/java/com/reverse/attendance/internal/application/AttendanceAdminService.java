package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.domain.AttendancePolicy;
import com.reverse.attendance.internal.dto.request.AttendanceMonthlyCloseRequest;
import com.reverse.attendance.internal.dto.request.AttendancePolicyUpsertRequest;
import com.reverse.attendance.internal.dto.response.AdminAttendanceDashboardResponse;
import com.reverse.attendance.internal.dto.response.AdminAttendanceReportResponse;
import com.reverse.attendance.internal.dto.response.AttendanceHistoryResponse;
import com.reverse.attendance.internal.dto.response.AttendanceMonthlyCloseResponse;
import com.reverse.attendance.internal.dto.response.AttendancePolicyResponse;
import com.reverse.attendance.internal.dto.response.AttendanceSummaryResponse;
import com.reverse.attendance.internal.persistence.AttendanceHistoryMapper;
import com.reverse.attendance.internal.persistence.AttendanceMapper;
import com.reverse.attendance.internal.persistence.AttendancePolicyMapper;
import com.reverse.attendance.internal.persistence.BusinessTripMapper;
import com.reverse.attendance.internal.persistence.LeaveMapper;
import com.reverse.attendance.internal.persistence.OvertimeMapper;
import com.reverse.attendance.internal.persistence.WeeklyWorkScheduleMapper;
import com.reverse.core.response.PageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceAdminService {

    private final AttendancePolicyMapper attendancePolicyMapper;
    private final AttendanceMapper attendanceMapper;
    private final AttendanceHistoryMapper attendanceHistoryMapper;
    private final AttendanceSyncService attendanceSyncService;
    private final LeaveMapper leaveMapper;
    private final OvertimeMapper overtimeMapper;
    private final BusinessTripMapper businessTripMapper;
    private final WeeklyWorkScheduleMapper weeklyWorkScheduleMapper;

    public AttendancePolicyResponse getPolicy(Long employeeId) {
        AttendancePolicy policy =
                attendancePolicyMapper
                        .findByEmployeeId(employeeId)
                        .orElseThrow(
                                () ->
                                        new com.reverse.core.exception.NotFoundException(
                                                "해당 사원의 근태 규정이 없습니다."));
        return AttendancePolicyResponse.from(policy);
    }

    @Transactional
    public AttendancePolicyResponse upsertPolicy(
            Long employeeId, AttendancePolicyUpsertRequest request) {
        validatePolicyTimes(request);

        AttendancePolicy policy =
                AttendancePolicy.builder()
                        .employeeId(employeeId)
                        .stdStartTime(request.getStdStartTime())
                        .stdEndTime(request.getStdEndTime())
                        .coreTimeStart(request.getCoreTimeStart())
                        .coreTimeEnd(request.getCoreTimeEnd())
                        .breakTimeStart(request.getBreakTimeStart())
                        .breakTimeEnd(request.getBreakTimeEnd())
                        .build();

        attendancePolicyMapper.upsert(policy);
        return getPolicy(employeeId);
    }

    public AdminAttendanceDashboardResponse getDashboard(int year, int month) {
        String targetMonth = formatTargetMonth(year, month);
        AttendanceSummaryResponse companySummary =
                attendanceMapper.countCompanyMonthlySummary(targetMonth);
        List<AdminAttendanceReportResponse> topRiskEmployees =
                attendanceMapper.findMonthlyEmployeeReports(targetMonth, year, 5, 0);

        return AdminAttendanceDashboardResponse.builder()
                .targetMonth(targetMonth)
                .companySummary(companySummary)
                .pendingLeaveCount(leaveMapper.countAll("PENDING"))
                .pendingOvertimeCount(overtimeMapper.countAll("PENDING"))
                .pendingBusinessTripCount(businessTripMapper.countAll("PENDING"))
                .pendingScheduleCount(weeklyWorkScheduleMapper.countAll("PENDING"))
                .topRiskEmployees(topRiskEmployees)
                .build();
    }

    public PageResponse<AdminAttendanceReportResponse> getMonthlyReport(
            int year, int month, int page, int size) {
        String targetMonth = formatTargetMonth(year, month);
        page = Math.max(1, page);
        size = Math.min(100, Math.max(1, size));
        int limit = size;
        long offsetLong = (long) (page - 1) * size;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new com.reverse.core.exception.BadRequestException("조회 가능한 페이지 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;

        List<AdminAttendanceReportResponse> content =
                attendanceMapper.findMonthlyEmployeeReports(targetMonth, year, limit, offset);
        long total = attendanceMapper.countMonthlyEmployeeReports(targetMonth);

        return PageResponse.of(content, page, size, total);
    }

    public PageResponse<AttendanceHistoryResponse> getHistory(
            int year, int month, Long employeeId, int page, int size) {
        String targetMonth = formatTargetMonth(year, month);
        page = Math.max(1, page);
        size = Math.min(100, Math.max(1, size));
        int limit = size;
        long offsetLong = (long) (page - 1) * size;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new com.reverse.core.exception.BadRequestException("조회 가능한 페이지 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;

        List<AttendanceHistoryResponse> content =
                attendanceHistoryMapper.findHistory(employeeId, targetMonth, limit, offset).stream()
                        .map(AttendanceHistoryResponse::from)
                        .collect(Collectors.toList());
        long total = attendanceHistoryMapper.countHistory(employeeId, targetMonth);
        return PageResponse.of(content, page, size, total);
    }

    @Transactional
    public AttendanceMonthlyCloseResponse closeMonth(AttendanceMonthlyCloseRequest request) {
        String targetMonth = formatTargetMonth(request.getYear(), request.getMonth());
        LocalDate firstDayOfMonth = LocalDate.of(request.getYear(), request.getMonth(), 1);
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        if (!firstDayOfMonth.isBefore(currentMonth)) {
            throw new com.reverse.core.exception.BadRequestException(
                    "월 근태 마감은 대상 월이 완전히 종료된 이후에만 가능합니다.");
        }

        int autoClosedCount =
                attendanceSyncService.autoCloseMissingCheckOutsByMonth(
                        targetMonth, "Monthly Close Auto Closed", "MONTHLY_AUTO_CLOSE");
        List<com.reverse.attendance.internal.domain.Attendance> beforeRecords =
                attendanceMapper.findAttendancesByMonth(targetMonth);
        int closedCount = attendanceMapper.closeMonthlyRecords(targetMonth);
        attendanceSyncService.recordCloseStateChanges(
                beforeRecords, targetMonth, false, true, "MONTHLY_CLOSE", "월 근태 마감 처리");

        return AttendanceMonthlyCloseResponse.builder()
                .targetMonth(targetMonth)
                .autoClosedCount(autoClosedCount)
                .closedCount(closedCount)
                .build();
    }

    @Transactional
    public AttendanceMonthlyCloseResponse reopenMonth(AttendanceMonthlyCloseRequest request) {
        String targetMonth = formatTargetMonth(request.getYear(), request.getMonth());
        List<com.reverse.attendance.internal.domain.Attendance> beforeRecords =
                attendanceMapper.findAttendancesByMonth(targetMonth);
        int reopenedCount = attendanceMapper.reopenMonthlyRecords(targetMonth);
        attendanceSyncService.recordCloseStateChanges(
                beforeRecords, targetMonth, true, false, "MONTHLY_REOPEN", "월 근태 마감 해제 처리");
        return AttendanceMonthlyCloseResponse.builder()
                .targetMonth(targetMonth)
                .autoClosedCount(0)
                .closedCount(reopenedCount)
                .build();
    }

    private String formatTargetMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("월은 1-12 사이여야 합니다.");
        }
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("유효하지 않은 연도입니다.");
        }
        return String.format("%04d-%02d", year, month);
    }

    private void validatePolicyTimes(AttendancePolicyUpsertRequest request) {
        if (!request.getStdStartTime().isBefore(request.getStdEndTime())) {
            throw new IllegalArgumentException("표준 출근시간은 표준 퇴근시간보다 빨라야 합니다.");
        }
        if (!request.getCoreTimeStart().isBefore(request.getCoreTimeEnd())) {
            throw new IllegalArgumentException("코어타임 시작은 종료보다 빨라야 합니다.");
        }
        if (!request.getBreakTimeStart().isBefore(request.getBreakTimeEnd())) {
            throw new IllegalArgumentException("휴게 시작은 종료보다 빨라야 합니다.");
        }
        if (request.getCoreTimeStart().isBefore(request.getStdStartTime())
                || request.getCoreTimeEnd().isAfter(request.getStdEndTime())) {
            throw new IllegalArgumentException("코어타임은 표준 근무시간 범위 안에 있어야 합니다.");
        }
        if (request.getBreakTimeStart().isBefore(request.getStdStartTime())
                || request.getBreakTimeEnd().isAfter(request.getStdEndTime())) {
            throw new IllegalArgumentException("휴게시간은 표준 근무시간 범위 안에 있어야 합니다.");
        }
    }
}
