package com.reverse.approval.internal.persistence.param;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ApprovalAttachmentParam {
    private final String fileKey;
    private final String filePath;
    private final String originalName;
    private final LocalDateTime createdDt;
    private final Long approvalId;

    @Builder
    public ApprovalAttachmentParam(
            String fileKey,
            String filePath,
            String originalName,
            LocalDateTime createdDt,
            Long approvalId) {
        this.fileKey = fileKey;
        this.filePath = filePath;
        this.originalName = originalName;
        this.createdDt = createdDt;
        this.approvalId = approvalId;
    }

    public static ApprovalAttachmentParam from(
            String fileKey, String filePath, String originalName, Long approvalId) {
        return ApprovalAttachmentParam.builder()
                .fileKey(fileKey)
                .filePath(filePath)
                .originalName(originalName)
                .createdDt(LocalDateTime.now())
                .approvalId(approvalId)
                .build();
    }
}
