package com.reverse.attendance.internal.application;

import com.reverse.attendance.AttendanceFacade;
import com.reverse.attendance.dto.request.LeaveBalanceInitializeRequest;
import com.reverse.attendance.dto.response.PayrollAttendanceResponse;
import com.reverse.attendance.internal.application.dto.response.AttendanceDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttendanceFacadeImpl implements AttendanceFacade {

    private final AttendanceService attendanceService;
    private final LeaveService leaveService;
    private final BusinessTripService businessTripService;
    private final OvertimeService overtimeService;

    @Override
    public AttendanceDashboardResponse getDashboardData(Long employeeId, int year, int month) {

        return AttendanceDashboardResponse.builder()
                .attendanceSummary(attendanceService.getMonthlySummary(employeeId, year, month))
                .leaveBalance(leaveService.getLeaveBalance(employeeId))
                .recentLeaveRequests(leaveService.getMyLeaveRequests(employeeId))
                .recentBusinessTrips(businessTripService.getMyTrips(employeeId))
                .recentOvertimes(overtimeService.getMyOvertimes(employeeId))
                .build();
    }

    @Override
    public PayrollAttendanceResponse getAttendanceForPayroll(Long employeeId, int year, int month) {
        // TODO: 나중에 근태 서비스들 조합해서 급여용 DTO 만드는 로직 작성!
        // 당장은 빨간 줄(에러)만 없애기 위해 일단 null을 리턴합니다.
        return null;
    }

    @Override
    public void initializeLeaveBalance(LeaveBalanceInitializeRequest request) {
        // TODO: 나중에 신규 입사자 연차 세팅하는 로직 작성!
    }

}
