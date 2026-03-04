package com.reverse.approval.internal.dto.request;

import com.reverse.approval.internal.domain.enums.VacationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class VacationRequest {

    @NotNull(message = "휴가 타입이 없을 수는 없습니다.")
    VacationType vacationType;

    @NotNull(message = "휴가 날짜가 없을 수는 없습니다.")
    LocalDateTime startDate;

    @NotNull(message = "휴가 날짜가 없을 수는 없습니다.")
    LocalDateTime endDate;

    @NotBlank(message = "사유가 없을 수는 없습니다.")
    String reason;
}
