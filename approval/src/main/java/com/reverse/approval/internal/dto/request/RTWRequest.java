package com.reverse.approval.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class RTWRequest {

    @NotNull(message = "복직 날짜가 없을 수는 없습니다.")
    LocalDate rtwDate;

    @NotBlank(message = "사유가 없을 수는 없습니다.")
    String reason;
}
