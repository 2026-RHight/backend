package com.reverse.approval.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FlexibleWorkRequest {
    @NotNull(message = "유연근무 시작 날짜가 없을 수는 없습니다.")
    LocalDateTime startDate;

    @NotNull(message = "유연근무 종료 날짜가 없을 수는 없습니다.")
    LocalDateTime endDate;

    @NotBlank(message = "사유가 없을 수는 없습니다.")
    String reason;
}
