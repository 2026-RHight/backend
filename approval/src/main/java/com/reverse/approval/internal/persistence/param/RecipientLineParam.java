package com.reverse.approval.internal.persistence.param;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RecipientLineParam {
    private final Long approvalId;
    private final Long receiverId;
    private final String receiverName;
    private final String receiverRank;

    @Builder
    public RecipientLineParam(
            Long approvalId, Long receiverId, String receiverName, String receiverRank) {
        this.approvalId = approvalId;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.receiverRank = receiverRank;
    }

    public static RecipientLineParam from(
            Long receiverId, Long approvalId, String receiverName, String receiverRank) {
        return RecipientLineParam.builder()
                .approvalId(approvalId)
                .receiverId(receiverId)
                .receiverName(receiverName)
                .receiverRank(receiverRank)
                .build();
    }
}
