package com.reverse.performance.internal.dto.request;

public record MonthlyScoreCreateRequest(Integer year, Integer month) {
    public MonthlyScoreCreateRequest withEmployeeId(Long employeeId) {
        return new MonthlyScoreCreateRequest(year, month);
    }
}
