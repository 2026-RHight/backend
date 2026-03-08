package com.reverse.attendance.internal.dto.request;

import com.reverse.attendance.internal.domain.enums.LeaveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LeaveApplyRequest {

    @NotNull private LocalDate startDate;

    @NotNull private LocalDate endDate;

    @NotNull private LeaveType leaveType;

    @NotBlank private String reason;
}
