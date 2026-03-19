package com.reverse.approval.internal.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApprovalMainSummaryResponse(
        int pendingCount,
        int inProgressCount,
        List<MainItem> pendingDocuments,
        List<MainItem> inProgressDocuments) {
    public record MainItem(
            Long approvalId,
            String title,
            String drafterName,
            LocalDateTime draftDate,
            LocalDateTime readDate) {}
}
