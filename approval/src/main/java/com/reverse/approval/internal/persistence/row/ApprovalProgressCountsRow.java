package com.reverse.approval.internal.persistence.row;

public record ApprovalProgressCountsRow(
        Integer allCount, Integer draftCount, Integer inProgressCount, Integer rejectedCount) {}
