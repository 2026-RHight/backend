package com.reverse.approval.internal.persistence.param;

import com.reverse.approval.internal.domain.enums.ApprovalStatus;
import com.reverse.approval.internal.dto.request.ApprovalLineRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ApprovalLineParam {
    private final Long approvalId;
    private final Byte approvalSeq;
    private final ApprovalStatus approvalStatus;
    private final Long approverId;
    private final String approverName;
    private final String approverRank;

    @Builder
    public ApprovalLineParam(
            Long approvalId,
            Byte approvalSeq,
            ApprovalStatus approvalStatus,
            Long approverId,
            String approverName,
            String approverRank
    ) {
        this.approvalId = approvalId;
        this.approvalSeq = approvalSeq;
        this.approvalStatus = approvalStatus;
        this.approverId = approverId;
        this.approverName = approverName;
        this.approverRank = approverRank;
    }

    public static ApprovalLineParam from(
            ApprovalLineRequest dto,
            Long approvalId,
            ApprovalStatus status,
            String approverName,
            String approverRank
    ) {
        return ApprovalLineParam.builder()
                .approvalId(approvalId)
                .approvalSeq(dto.getApprovalSeq())
                .approvalStatus(status)
                .approverId(dto.getApproverId())
                .approverName(approverName == null ? "미지정" : approverName)
                .approverRank(approverRank == null ? "미지정" : approverRank)
                .build();
    }
}
