package com.reverse.approval.internal.dto.response;

public record DownloadedApprovalFile(String originalName, byte[] content) {}
