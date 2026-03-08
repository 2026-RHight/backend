package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record ApprovalProgressRow(
        Long approvalId,
        String docId,
        String docType,
        String title,
        String approvalStatus,
        LocalDateTime draftDate,
        LocalDateTime readDate,
        String currentApproverName,
        Long totalApproverCount,
        Long doneApproverCount,
        Integer progressPercent) {}
