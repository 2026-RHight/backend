package com.reverse.performance.internal.dto.request;

public record MonthlyScoreCreateRequest(
        Long employeeId,
        Integer year,
        Integer month
) {
    public MonthlyScoreCreateRequest withEmployeeId(Long employeeId) {
        return new MonthlyScoreCreateRequest(employeeId, year, month);
    }
}
