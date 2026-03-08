package com.reverse.approval.internal.persistence.row;

public record ApprovalAttachmentRow(
        Long fileId, String filePath, String originalName, Long approvalId) {}
