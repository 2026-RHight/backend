package com.reverse.performance.internal.dto.response;

import java.util.List;

public record PerformanceTeamStatsMemberResponse(
        Long id,
        String name,
        String role,
        String department,
        double avgScore,
        int systemScore,
        List<PerformanceTeamStatChartItemResponse> chartData,
        List<PerformanceTeamStatTaskResponse> tasks) {}
