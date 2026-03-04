package com.reverse.attendance.dto.request;

import com.reverse.attendance.internal.domain.enums.AttendanceStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class AttendanceModifyRequest {

    private  Long targetEmployeeId;
    private LocalDate workDate;

    // 변경ㅇ될 수 있는 데이터
    private LocalTime newCheckInTime;
    private LocalTime newCheckOutTime;
    private AttendanceStatus newStatus;

    private String  modifyReason;

}
