package com.reverse.approval.internal.persistence.param;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ReferenceLineParam {
    private final Long approvalId;
    private final Long referencerId;
    private final String referencerName;
    private final String referenceRank;

    @Builder
    public ReferenceLineParam(Long approvalId, Long referencerId, String referencerName, String referenceRank) {
        this.approvalId = approvalId;
        this.referencerId = referencerId;
        this.referencerName = referencerName;
        this.referenceRank = referenceRank;
    }

    public static ReferenceLineParam from(
            Long referencerId,
            Long approvalId,
            String referencerName,
            String referenceRank
    ) {
        return ReferenceLineParam.builder()
                .approvalId(approvalId)
                .referencerId(referencerId)
                .referencerName(referencerName)
                .referenceRank(referenceRank)
                .build();
    }
}
