package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record ApprovalHeaderRow(
        Long approvalId,
        String docType,
        String docId,
        String title,
        String approvalStatus,
        LocalDateTime draftDate,
        Long drafterId,
        String drafterName,
        String departmentName,
        LocalDateTime approveDate) {}
