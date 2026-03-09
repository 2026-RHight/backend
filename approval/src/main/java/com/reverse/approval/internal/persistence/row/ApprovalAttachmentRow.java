package com.reverse.approval.internal.persistence.row;

import java.time.LocalDateTime;

public record ApprovalAttachmentRow(
        Long fileId,
        String fileKey,
        String filePath,
        String originalName,
        LocalDateTime createdDate,
        Long approvalId) {}
