package com.reverse.approval.internal.persistence.param;

import com.reverse.approval.internal.dto.request.FlexibleWorkRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FlexibleWorkParam {
    private final Long approvalId;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String reason;

    @Builder
    public FlexibleWorkParam(Long approvalId, LocalDateTime startDate, LocalDateTime endDate, String reason) {
        this.approvalId = approvalId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    public static FlexibleWorkParam from(FlexibleWorkRequest dto, Long approvalId) {
        return FlexibleWorkParam.builder()
                .approvalId(approvalId)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .build();
    }
}
