package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record ApprovalBoxRow(
        Long approvalId,
        String docId,
        String docType,
        String title,
        String approvalStatus,
        LocalDateTime draftDate,
        LocalDateTime approveDate,
        Long drafterId,
        String drafterName,
        String departmentName,
        LocalDateTime readDate) {}
