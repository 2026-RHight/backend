package com.reverse.attendance.internal.dto.response;

import com.reverse.attendance.internal.domain.enums.AttendanceStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDailyAttendanceResponse {

    private Long attendanceId;
    private Long employeeId;
    private String employeeName;
    private String departmentName;
    private String positionName;
    private LocalDate workDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private AttendanceStatus status;
    private String tardyReason;
    private String modifyReason;
    private boolean closed;
}
