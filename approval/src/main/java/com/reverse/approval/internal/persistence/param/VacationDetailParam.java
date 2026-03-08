package com.reverse.approval.internal.persistence.param;

import com.reverse.approval.internal.domain.enums.VacationType;
import com.reverse.approval.internal.dto.request.VacationRequest;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class VacationDetailParam {
    private final Long approvalId;
    private final VacationType vacationType;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String reason;

    @Builder
    public VacationDetailParam(
            Long approvalId,
            VacationType vacationType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String reason) {
        this.approvalId = approvalId;
        this.vacationType = vacationType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    public static VacationDetailParam from(VacationRequest dto, Long approvalId) {
        return VacationDetailParam.builder()
                .approvalId(approvalId)
                .vacationType(dto.getVacationType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .build();
    }
}
