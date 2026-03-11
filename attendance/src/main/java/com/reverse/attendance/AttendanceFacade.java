package com.reverse.attendance;

import com.reverse.attendance.dto.LeaveBalanceInitializeRequest;
import com.reverse.attendance.dto.PayrollAttendanceResponse;
import com.reverse.attendance.internal.dto.response.AttendanceDashboardResponse;

/** 근태 모듈의 기능을 외부에 제공하는 퍼블릭 인터페이스입니다. */
public interface AttendanceFacade {

    /* 프론트엔드 대시보드 렌더링을 위한 종합 데이터를 반환합니다. */
    AttendanceDashboardResponse getDashboardData(Long employeeId, int year, int month);

    //  급여 모듈에서 호출할 메서드
    PayrollAttendanceResponse getAttendanceForPayroll(Long employeeId, int year, int month);

    // 인사 모듈에서 호출할 메서드
    void initializeLeaveBalance(LeaveBalanceInitializeRequest request);
}
