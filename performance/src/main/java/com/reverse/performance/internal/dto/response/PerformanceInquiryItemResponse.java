package com.reverse.performance.internal.dto.response;

public record PerformanceInquiryItemResponse(
        Long id,
        String type,
        String title,
        String coreTask,
        String date,
        String status,
        Integer progress,
        String employeeName,
        Long employeeId,
        String description,
        String achievement) {}
