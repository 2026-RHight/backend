package com.reverse.approval.internal.persistence.param;

import com.reverse.approval.internal.dto.request.RTWRequest;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RTWDetailParam {
    private final Long approvalId;
    private final LocalDate rtwDate;
    private final String reason;

    @Builder
    public RTWDetailParam(Long approvalId, LocalDate rtwDate, String reason) {
        this.approvalId = approvalId;
        this.rtwDate = rtwDate;
        this.reason = reason;
    }

    public static RTWDetailParam from(RTWRequest dto, Long approvalId) {
        return RTWDetailParam.builder()
                .approvalId(approvalId)
                .rtwDate(dto.getRtwDate())
                .reason(dto.getReason())
                .build();
    }
}
