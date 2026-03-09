package com.reverse.approval.internal.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApprovalReviewPageResponse(
        List<ApprovalReviewItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {
    public record ApprovalReviewItem(
            Long approvalId,
            String docId,
            String docType,
            String title,
            String approvalStatus,
            String drafterName,
            String departmentName,
            LocalDateTime draftDate) {}
}
