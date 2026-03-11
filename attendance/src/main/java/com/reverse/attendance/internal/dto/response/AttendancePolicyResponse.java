package com.reverse.attendance.internal.dto.response;

import com.reverse.attendance.internal.domain.AttendancePolicy;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendancePolicyResponse {

    private Long policyId;
    private Long employeeId;
    private LocalTime stdStartTime;
    private LocalTime stdEndTime;
    private LocalTime coreTimeStart;
    private LocalTime coreTimeEnd;
    private LocalTime breakTimeStart;
    private LocalTime breakTimeEnd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AttendancePolicyResponse from(AttendancePolicy policy) {
        return AttendancePolicyResponse.builder()
                .policyId(policy.getPolicyId())
                .employeeId(policy.getEmployeeId())
                .stdStartTime(policy.getStdStartTime())
                .stdEndTime(policy.getStdEndTime())
                .coreTimeStart(policy.getCoreTimeStart())
                .coreTimeEnd(policy.getCoreTimeEnd())
                .breakTimeStart(policy.getBreakTimeStart())
                .breakTimeEnd(policy.getBreakTimeEnd())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
