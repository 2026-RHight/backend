package com.reverse.approval.internal.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApprovalProgressPageResponse(
        List<ApprovalProgressItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {
    public record ApprovalProgressItem(
            Long approvalId,
            String docId,
            String docType,
            String title,
            String approvalStatus,
            LocalDateTime draftDate,
            LocalDateTime readDate,
            String currentApproverName,
            int totalApproverCount,
            int doneApproverCount,
            int progressPercent) {}
}
