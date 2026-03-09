package com.reverse.approval.internal.persistence.row;

public record ApprovalDashboardMyDraftRow(
        Long approvalId,
        String docType,
        String title,
        String currentApproverName,
        String approvalStatus) {}
