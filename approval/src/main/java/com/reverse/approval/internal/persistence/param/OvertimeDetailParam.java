package com.reverse.approval.internal.persistence.param;

import com.reverse.approval.internal.dto.request.OvertimeRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class OvertimeDetailParam {
    private final Long approvalId;
    private final LocalDate workDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String reason;

    @Builder
    public OvertimeDetailParam(Long approvalId, LocalDate workDate, LocalTime startTime, LocalTime endTime, String reason) {
        this.approvalId = approvalId;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
    }

    public static OvertimeDetailParam from(OvertimeRequest dto, Long approvalId) {
        return OvertimeDetailParam.builder()
                .approvalId(approvalId)
                .workDate(dto.getWorkDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .reason(dto.getReason())
                .build();
    }
}
