package com.reverse.attendance.internal.dto.response;

import lombok.Builder;

@Builder
public record AttendanceRequestHistoryItemResponse(
        Long id,
        String type,
        String title,
        String period,
        String reason,
        String status,
        String appliedAt,
        String targetDate,
        String approver,
        String rejectReason) {}
