package com.reverse.attendance.internal.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeaveBalanceResponse {

    private double totalAnnualLeave; // 총 발생 연차
    private double usedAnnualLeave; // 승인된 사용 연차
    private double pendingAnnualLeave; // 결재 대기 중인 연차 (마이너스 표시용)
    private double remainingAnnualLeave; // 잔여 연차
}
