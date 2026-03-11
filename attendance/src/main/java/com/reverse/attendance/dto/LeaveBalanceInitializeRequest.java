package com.reverse.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LeaveBalanceInitializeRequest {

    private Long employeeId;
    private double defaultAnnualLeaveDays; // 부여할 기본 연차 일수
}
