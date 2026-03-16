package com.reverse.approval.internal.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApprovalVacationPageResponse(
        List<ApprovalVacationItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {
    public record ApprovalVacationItem(
            Long approvalId,
            String docId,
            String docType,
            String approvalStatus,
            Long drafterId,
            String drafterName,
            String departmentName,
            String vacationType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            double days,
            String reason,
            LocalDateTime draftDate) {}
}
