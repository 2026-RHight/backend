package com.reverse.performance.internal.dto.request;

public record PerformanceCreateDTO(
        PerformanceRequest request,
        PerformancePersonalRequest personalRequest,
        PerformanceTeamRequest teamRequest
) {
    public PerformanceCreateDTO withEmployeeId(Long employeeId) {
        return new PerformanceCreateDTO(
                request == null ? null : request.withEmployeeId(employeeId),
                personalRequest,
                teamRequest
        );
    }
}
