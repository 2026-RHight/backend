package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record ApprovalLineDetailRow(
        Byte approvalSeq,
        String approvalStatus,
        Long approverId,
        String approverName,
        String approverRank,
        String reason,
        LocalDateTime approvedDate,
        LocalDateTime readDate) {}
