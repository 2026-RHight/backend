package com.reverse.approval.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class BusinessTripRequest {

    @NotNull(message = "유형이 없을 수는 없습니다.")
    @Pattern(regexp = "^(OUTSIDE|BUSINESSTRIP)$")
    String tripType;

    @NotBlank(message = "목적지가 없을 수는 없습니다.")
    String destination;

    @NotNull(message = "외근/출장 날짜가 없을 수는 없습니다.")
    LocalDateTime startDate;

    @NotNull(message = "외근/출장 날짜가 없을 수는 없습니다.")
    LocalDateTime endDate;

    @NotBlank(message = "사유가 없을 수는 없습니다.")
    String reason;
}
