package com.reverse.performance.internal.dto.response;

import java.util.List;

public record PerformanceTeamStatsResponse(
        List<String> teamOptions, List<PerformanceTeamStatsMemberResponse> members) {}
