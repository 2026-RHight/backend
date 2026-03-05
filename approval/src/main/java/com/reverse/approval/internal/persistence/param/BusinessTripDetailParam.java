package com.reverse.approval.internal.persistence.param;

import com.reverse.approval.internal.dto.request.BusinessTripRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BusinessTripDetailParam {
    private final Long approvalId;
    private final String tripType;
    private final String destination;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String reason;

    @Builder
    public BusinessTripDetailParam(Long approvalId, String tripType, String destination, LocalDateTime startDate, LocalDateTime endDate, String reason) {
        this.approvalId = approvalId;
        this.tripType = tripType;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    public static BusinessTripDetailParam from(BusinessTripRequest dto, Long approvalId) {
        return BusinessTripDetailParam.builder()
                .approvalId(approvalId)
                .tripType(dto.getTripType())
                .destination(dto.getDestination())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .build();
    }
}
