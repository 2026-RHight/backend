package com.reverse.performance.internal.dto.request;

import jakarta.validation.Valid;

public record PerformanceCreateDTO(
        @Valid PerformanceRequest request,
        @Valid PerformancePersonalRequest personalRequest,
        @Valid PerformanceTeamRequest teamRequest) {
    public PerformanceCreateDTO withEmployeeId(Long employeeId) {
        return new PerformanceCreateDTO(
                request == null ? null : request.withEmployeeId(employeeId),
                personalRequest,
                teamRequest);
    }
}
