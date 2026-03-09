package com.reverse.approval.internal.dto.response;

public record ApprovalProgressOverviewResponse(Counts counts, ApprovalProgressPageResponse page) {
    public record Counts(int allCount, int draftCount, int inProgressCount, int rejectedCount) {}
}
