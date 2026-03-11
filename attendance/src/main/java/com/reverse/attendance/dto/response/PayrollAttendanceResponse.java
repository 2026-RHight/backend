package com.reverse.attendance.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollAttendanceResponse {

    private Long employeeId;
    private int year;
    private int month;

    // 1. 기본급 계산용
    private int normalWorkDays; // 정상 근무 일수
    private BigDecimal totalNormalWorkHours; // 정상 근무 총 시간

    // 2. 추가 수당 계산용
    private BigDecimal totalOvertimeHours; // 연장근무 총 시간
    private BigDecimal nightWorkHours; // 야간근무 총 시간
    private BigDecimal holidayWorkHours; // 휴일근무 총 시간
    private int businessTripDays; // 출장 일수

    // 3. 급여 차감용
    private int tardyCount; // 지각 횟수
    private int earlyLeaveCount; // 조퇴 횟수
    private int absentDays; // 무단 결근 일수

    // 4. 휴가 관련
    private BigDecimal paidLeaveDays; // 유급 휴가 사용 일수
    private BigDecimal unpaidLeaveDays; // 무급 휴가 사용 일수
}
