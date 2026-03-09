package com.reverse.approval.internal.persistence.row;

public record ApprovalDashboardCountsRow(
        Integer pendingReviewCount, Integer inProgressCount, Integer completedThisMonthCount) {}
