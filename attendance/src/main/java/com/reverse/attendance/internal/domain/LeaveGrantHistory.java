package com.reverse.attendance.internal.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeaveGrantHistory {

    private Long grantHistoryId;
    private Long employeeId;
    private Integer baseYear;
    private LocalDate grantDate;
    private BigDecimal grantDays;
    private String grantType;
    private String reason;
    private LocalDateTime createdAt;
}
