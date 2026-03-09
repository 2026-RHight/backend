package com.reverse.approval.internal.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApprovalDashboardResponse(
        Counts counts, List<PendingReviewItem> pendingReviewDocuments, List<MyDraftItem> myDrafts) {
    public record Counts(
            int pendingReviewCount, int inProgressCount, int completedThisMonthCount) {}

    public record PendingReviewItem(
            Long approvalId,
            String docType,
            String title,
            String drafterName,
            LocalDateTime draftDate,
            LocalDateTime readDate) {}

    public record MyDraftItem(
            Long approvalId,
            String docType,
            String title,
            String currentApproverName,
            String approvalStatus) {}
}
