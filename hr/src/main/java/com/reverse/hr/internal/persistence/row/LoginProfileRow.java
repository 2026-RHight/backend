package com.reverse.hr.internal.persistence.row;

/** 로그인 시 employee/인사정보 조인으로 조회되는 프로필 요약 Row. */
public record LoginProfileRow(
        Long employeeId,
        String employeeNum,
        String employeeName,
        String orgName,
        String positionName,
        String rankName,
        String jobName) {}
