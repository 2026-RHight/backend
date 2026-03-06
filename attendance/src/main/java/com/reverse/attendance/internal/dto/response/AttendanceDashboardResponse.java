package com.reverse.attendance.internal.dto.response;

import com.reverse.attendance.internal.domain.BusinessTrip;
import com.reverse.attendance.internal.domain.LeaveRequest;
import com.reverse.attendance.internal.domain.Overtime;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AttendanceDashboardResponse {

    // 이번 달 근태 요약
    private AttendanceSummaryResponse attendanceSummary;

    // 내 연차 현황
    private LeaveBalanceResponse leaveBalance;

    // 최근 나의 휴가 신청 내역
    private List<LeaveRequest> recentLeaveRequests;

    // 최근 나의 외근/출장 내역
    private List<BusinessTrip> recentBusinessTrips;

    // 최근 나의 연장근무 내역
    private List<Overtime> recentOvertimes;
}