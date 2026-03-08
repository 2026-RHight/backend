package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record ApprovalDashboardPendingReviewRow(
        Long approvalId,
        String docType,
        String title,
        String drafterName,
        LocalDateTime draftDate,
        LocalDateTime readDate) {}
