package com.reverse.performance.internal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.reverse.performance.internal.domain.Status;
import com.reverse.performance.internal.domain.WorkItem;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceRequest {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long performanceId;

    private String title;
    private WorkItem workItem;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private String workDetail;
    private Status status;
    private Integer achievementRate;
    private Integer difficultyScore;
    private String comment;
    private String feedback;

    public PerformanceRequest withEmployeeId(Long employeeId) {
        return this;
    }
}
