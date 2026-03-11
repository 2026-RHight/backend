package com.reverse.attendance.internal.domain;

import com.reverse.attendance.internal.domain.enums.AttendanceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Boolean closed;
    private BigDecimal overtimeHours;
    private BigDecimal nightWorkHours;
    private BigDecimal holidayWorkHours;
    private Boolean unpaidLeave;

    @Builder
    public Attendance(
            Long attendanceId,
            Long employeeId,
            LocalDate workDate,
            LocalTime checkInTime,
            LocalTime checkOutTime,
            AttendanceStatus status,
            String tardyReason,
            String modifyReason,
            Boolean closed,
            BigDecimal overtimeHours,
            BigDecimal nightWorkHours,
            BigDecimal holidayWorkHours,
            Boolean unpaidLeave) {
        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.tardyReason = tardyReason;
        this.modifyReason = modifyReason;
        this.closed = closed;
        this.overtimeHours = overtimeHours;
        this.nightWorkHours = nightWorkHours;
        this.holidayWorkHours = holidayWorkHours;
        this.unpaidLeave = unpaidLeave;
    }
}
