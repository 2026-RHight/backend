package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record ApprovalMainItemRow(
        Long approvalId,
        String title,
        String drafterName,
        LocalDateTime draftDate,
        LocalDateTime readDate) {}
