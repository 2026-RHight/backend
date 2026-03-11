package com.reverse.performance.internal.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PerformanceRegistrationRequest(
        @Size(max = 20, message = "type은 20자를 초과할 수 없습니다.") String type,
        LocalDate startDate,
        LocalDate endDate,
        @NotNull(message = "difficultyScore는 필수입니다.")
                @Min(value = 1, message = "difficultyScore는 1 이상이어야 합니다.")
                @Max(value = 5, message = "difficultyScore는 5 이하여야 합니다.")
                Integer difficultyScore,
        @NotBlank(message = "title은 필수입니다.") @Size(max = 255, message = "title은 255자를 초과할 수 없습니다.")
                String title,
        @Size(max = 1000, message = "coreTask는 1000자를 초과할 수 없습니다.") String coreTask,
        @Size(max = 5000, message = "content는 5000자를 초과할 수 없습니다.") String content,
        @Size(max = 2000, message = "value는 2000자를 초과할 수 없습니다.") String value) {}
