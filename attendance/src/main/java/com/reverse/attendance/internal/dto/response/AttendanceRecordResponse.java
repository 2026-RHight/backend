package com.reverse.attendance.internal.dto.response;

import com.reverse.attendance.internal.domain.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class AttendanceRecordResponse {

    private Long attendanceId;
    private LocalDate workDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private AttendanceStatus status;
    private String statusDescription;   // 화면에 "정상", "지각"등 바로 보여주기위한 필드

}
