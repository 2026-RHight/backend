package com.reverse.approval.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class OvertimeRequest {

    @NotNull(message = "날짜가 없을 수는 없습니다.")
    LocalDate workDate;

    @NotNull(message = "시간이 없을 수는 없습니다.")
    LocalTime startTime;

    @NotNull(message = "시간이 없을 수는 없습니다.")
    LocalTime endTime;

    @NotBlank(message = "사유가 없을 수는 없습니다.")
    String reason;

}
