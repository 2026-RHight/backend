package com.reverse.approval.internal.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApprovalFlexiblePageResponse(
        List<ApprovalFlexibleItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {
    public record ApprovalFlexibleItem(
            Long approvalId,
            String docId,
            String docType,
            String approvalStatus,
            Long drafterId,
            String drafterName,
            String departmentName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String reason,
            LocalDateTime draftDate) {}
}
