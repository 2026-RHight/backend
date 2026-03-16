package com.reverse.attendance.internal.dto.response;

import lombok.Builder;

@Builder
public record AttendanceVacationHistoryItemResponse(
        Long id,
        String leaveType,
        String startDate,
        String endDate,
        double usedDays,
        String status,
        String applyDate,
        String rejectReason) {}
