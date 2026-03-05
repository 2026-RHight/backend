package com.reverse.attendance.internal.dto.request;

import com.reverse.attendance.internal.domain.enums.TripType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BusinessTripApplyRequest {     /* 신청용 */

    private Long employeeId;
    private TripType tripType;
    private String destination;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String reason;

}
