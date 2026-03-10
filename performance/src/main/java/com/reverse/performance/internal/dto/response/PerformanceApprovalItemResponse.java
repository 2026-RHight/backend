package com.reverse.performance.internal.dto.response;

public record PerformanceApprovalItemResponse(
        Long id,
        String user,
        String dept,
        String title,
        String date,
        String status,
        String type,
        String achievement,
        Integer progress) {}
