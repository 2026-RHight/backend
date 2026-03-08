package com.reverse.approval.internal.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApprovalBoxPageResponse(
        List<ApprovalBoxItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {
    public record ApprovalBoxItem(
            Long approvalId,
            String docId,
            String docType,
            String title,
            String approvalStatus,
            LocalDateTime draftDate,
            LocalDateTime approveDate,
            Long drafterId,
            String drafterName,
            String departmentName,
            LocalDateTime readDate) {}
}
