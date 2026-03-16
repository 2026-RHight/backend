package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record ApprovalVacationRow(
        Long approvalId,
        String docId,
        String docType,
        String approvalStatus,
        Long drafterId,
        String drafterName,
        String departmentName,
        String vacationType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String reason,
        LocalDateTime draftDate) {}
