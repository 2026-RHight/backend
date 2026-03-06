package com.reverse.attendance.internal.dto.request;

import com.reverse.attendance.internal.domain.enums.TripType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BusinessTripApplyRequest { /* 신청용 */

    @NotNull
    private TripType tripType;

    @NotBlank
    private String destination;

    @NotNull
    private LocalDateTime startDatetime;

    @NotNull
    private LocalDateTime endDatetime;

    @NotBlank
    private String reason;

}
