package com.reverse.attendance.internal.dto.response;

import com.reverse.attendance.internal.domain.LeaveGrantHistory;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeaveGrantHistoryResponse {

    private Long grantHistoryId;
    private Integer baseYear;
    private LocalDate grantDate;
    private BigDecimal grantDays;
    private String grantType;
    private String reason;

    public static LeaveGrantHistoryResponse from(LeaveGrantHistory history) {
        return LeaveGrantHistoryResponse.builder()
                .grantHistoryId(history.getGrantHistoryId())
                .baseYear(history.getBaseYear())
                .grantDate(history.getGrantDate())
                .grantDays(history.getGrantDays())
                .grantType(history.getGrantType())
                .reason(history.getReason())
                .build();
    }
}
