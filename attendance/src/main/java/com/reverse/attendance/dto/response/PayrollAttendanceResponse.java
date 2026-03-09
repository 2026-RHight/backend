package com.reverse.attendance.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayrollAttendanceResponse {

    private Long employeeId;
    private int year;
    private int month;

    // 1. 기본급 계산용 (Base Pay)
    private int normalWorkDays; // 정상 근무 일수
    private double totalNormalWorkHours; // 정상 근무 총 시간 (시급제 계산이나 정확한 통계용)

    // 2. 추가 수당 계산용 (Extra Pay)
    private double totalOvertimeHours; // 연장근무 총 시간 (1.5배 수당)
    private double nightWorkHours; // 야간근무 총 시간 (밤 10시 ~ 새벽 6시, 추가 수당 발생)
    private double holidayWorkHours; // 휴일근무 총 시간 (주말/공휴일 출근, 1.5배 또는 2배 수당)
    private int businessTripDays; // 출장 일수 (출장비, 식대 등 정액 수당 지급용)

    // 3. 급여 차감용 (Deductions)
    private int tardyCount; // 지각 횟수
    private int earlyLeaveCount; // 조퇴 횟수 (지각과 묶어서 페널티를 주는 회사들이 많음)
    private int absentDays; // 무단 결근 일수 (일할 계산해서 급여 대폭 삭감)

    // 4. 휴가 관련 (Leaves)
    private double paidLeaveDays; // 유급 휴가 사용 일수 (연차, 포상휴가 등 - 급여 삭감 없음)
    private double unpaidLeaveDays; // 무급 휴가 사용 일수 (가족돌봄휴가 무급분 등 - 급여 삭감 됨)
}
