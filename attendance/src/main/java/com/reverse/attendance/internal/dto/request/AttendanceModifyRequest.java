package com.reverse.attendance.internal.dto.request;

import com.reverse.attendance.internal.domain.enums.AttendanceStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendanceModifyRequest {

    private Long targetEmployeeId;
    private LocalDate workDate;

    // 변경ㅇ될 수 있는 데이터
    private LocalTime newCheckInTime;
    private LocalTime newCheckOutTime;
    private AttendanceStatus newStatus;

    @Size(max = 255)
    private String newTardyReason;

    private String modifyReason;
}
