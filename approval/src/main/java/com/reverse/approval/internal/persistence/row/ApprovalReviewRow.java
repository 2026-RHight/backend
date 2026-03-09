package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record ApprovalReviewRow(
        Long approvalId,
        String docId,
        String docType,
        String title,
        String approvalStatus,
        String drafterName,
        String departmentName,
        LocalDateTime draftDate) {}
