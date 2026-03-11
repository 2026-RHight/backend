package com.reverse.performance.internal.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PerformanceTeamEvaluationSubmitRequest(
        @NotNull(message = "appraiseeId는 필수입니다.") Long appraiseeId,
        @NotNull(message = "performanceScore는 필수입니다.")
                @Min(value = 1, message = "performanceScore는 1 이상이어야 합니다.")
                @Max(value = 5, message = "performanceScore는 5 이하여야 합니다.")
                Integer performanceScore,
        String performanceComment,
        @NotNull(message = "attitudeScore는 필수입니다.")
                @Min(value = 1, message = "attitudeScore는 1 이상이어야 합니다.")
                @Max(value = 5, message = "attitudeScore는 5 이하여야 합니다.")
                Integer attitudeScore,
        String attitudeComment,
        @NotNull(message = "collaborationScore는 필수입니다.")
                @Min(value = 1, message = "collaborationScore는 1 이상이어야 합니다.")
                @Max(value = 5, message = "collaborationScore는 5 이하여야 합니다.")
                Integer collaborationScore,
        String collaborationComment,
        @NotNull(message = "creativityScore는 필수입니다.")
                @Min(value = 1, message = "creativityScore는 1 이상이어야 합니다.")
                @Max(value = 5, message = "creativityScore는 5 이하여야 합니다.")
                Integer creativityScore,
        String creativityComment) {}
