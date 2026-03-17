package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record ApprovalFlexibleRow(
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
