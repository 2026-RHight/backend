package com.reverse.attendance.internal.dto.response;

import com.reverse.attendance.internal.domain.BusinessTrip;
import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import com.reverse.attendance.internal.domain.enums.TripType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessTripResponse {

    private Long tripId;
    private Long employeeId;
    private TripType tripType;
    private String destination;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String reason;
    private ApprovalStatus approvalStatus;
    private String rejectReason;

    public static BusinessTripResponse from(BusinessTrip trip) {
        return BusinessTripResponse.builder()
                .tripId(trip.getTripId())
                .employeeId(trip.getEmployeeId())
                .tripType(trip.getTripType())
                .destination(trip.getDestination())
                .startDatetime(trip.getStartDatetime())
                .endDatetime(trip.getEndDatetime())
                .reason(trip.getReason())
                .approvalStatus(trip.getApprovalStatus())
                .rejectReason(trip.getRejectReason())
                .build();
    }
}
