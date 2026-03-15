package com.reverse.hr.internal.persistence.row;

public record TeamBirthdayRow(
        Long employeeId, String employeeName, String birthday, Integer daysRemaining) {}
