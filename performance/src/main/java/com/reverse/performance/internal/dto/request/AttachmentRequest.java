package com.reverse.performance.internal.dto.request;

import java.time.LocalDateTime;

public record AttachmentRequest(
        Long attachmentId,
        Long performanceId,
        String fileName,
        String fileUrl,
        LocalDateTime confirmedAt,
        LocalDateTime createdAt
) {
}
