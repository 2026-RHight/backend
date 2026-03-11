package com.reverse.attendance.internal.application;

import com.reverse.attendance.AttendanceFacade;
import com.reverse.attendance.dto.LeaveBalanceInitializeRequest;
import com.reverse.attendance.dto.PayrollAttendanceResponse;
import com.reverse.attendance.internal.dto.response.AttendanceDashboardResponse;
import com.reverse.attendance.internal.persistence.AttendanceMapper;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttendanceFacadeImpl implements AttendanceFacade {

    private final AttendanceMapper attendanceMapper;
    private final AttendanceService attendanceService;
    private final LeaveService leaveService;
    private final BusinessTripService businessTripService;
    private final OvertimeService overtimeService;

    @Override
    public AttendanceDashboardResponse getDashboardData(Long employeeId, int year, int month) {

        return AttendanceDashboardResponse.builder()
                .attendanceSummary(attendanceService.getMonthlySummary(employeeId, year, month))
                .leaveBalance(leaveService.getLeaveBalance(employeeId))
                .recentLeaveRequests(leaveService.getMyLeaveRequests(employeeId, 1, 5).getContent())
                .recentBusinessTrips(businessTripService.getMyTrips(employeeId, 1, 5).getContent())
                .recentOvertimes(overtimeService.getMyOvertimes(employeeId, 1, 5).getContent())
                .build();
    }

    @Override
    public PayrollAttendanceResponse getAttendanceForPayroll(Long employeeId, int year, int month) {
        // 급여 계산에 필요한 종합적인 근태 통계 정보를 AttendanceMapper를 통해 조회합니다.
        PayrollAttendanceResponse response =
                attendanceMapper.getAttendanceForPayroll(employeeId, year, month);

        // 데이터가 아예 없을 경우 (해당 월 정상 출근이 0일이라도) 기본값이 세팅된 객체 반환
        if (response == null) {
            return PayrollAttendanceResponse.builder()
                    .employeeId(employeeId)
                    .year(year)
                    .month(month)
                    .totalNormalWorkHours(BigDecimal.ZERO)
                    .totalOvertimeHours(BigDecimal.ZERO)
                    .nightWorkHours(BigDecimal.ZERO)
                    .holidayWorkHours(BigDecimal.ZERO)
                    .paidLeaveDays(BigDecimal.ZERO)
                    .unpaidLeaveDays(BigDecimal.ZERO)
                    .build();
        }

        return response;
    }

    @Override
    public void initializeLeaveBalance(LeaveBalanceInitializeRequest request) {
        // TODO: 나중에 신규 입사자 연차 세팅하는 로직 작성!
    }
}
