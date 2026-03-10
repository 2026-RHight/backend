package com.reverse.performance.internal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PerformanceTeamRequest(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long performanceId,
        @NotNull(message = "weight는 필수입니다.")
                @Min(value = 0, message = "weight는 0 이상이어야 합니다.")
                @Max(value = 100, message = "weight는 100 이하여야 합니다.")
                Integer weight,
        @Size(max = 2000, message = "teamResultSummary는 2000자를 초과할 수 없습니다.")
                String teamResultSummary,
        @Size(max = 2000, message = "specialPoint는 2000자를 초과할 수 없습니다.") String specialPoint) {
    public PerformanceTeamRequest withPerformanceId(Long performanceId) {
        return new PerformanceTeamRequest(performanceId, weight, teamResultSummary, specialPoint);
    }
}
