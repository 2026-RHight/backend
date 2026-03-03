package com.reverse.attendance.internal.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class Attendance {

    private Long attendanceId;
    private Long employeeId;
    private LocalDate workDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private AttendanceStatus status;
    private String tardyReason;
    private String modifyReason;

    @Builder
    public Attendance(Long attendanceId
            , Long employeeId
            , LocalDate workDate
            , LocalTime checkInTime
            , LocalTime checkOutTime
            , AttendanceStatus status
            , String tardyReason
            , String modifyReason){

        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.tardyReason = tardyReason;
        this.modifyReason = modifyReason;
    }
}
