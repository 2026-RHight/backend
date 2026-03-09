package com.reverse.approval.internal.dto.request;

import com.reverse.approval.internal.domain.enums.LeaveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class LeaveRequest {
    @NotNull(message = "휴가 날짜가 없을 수는 없습니다.")
    LocalDateTime startDate;

    @NotNull(message = "휴가 날짜가 없을 수는 없습니다.")
    LocalDateTime endDate;

    @NotNull(message = "휴직 타입이 없을 수는 없습니다.")
    LeaveType leaveType;

    @NotBlank(message = "사유가 없을 수는 없습니다.")
    String reason;
}
