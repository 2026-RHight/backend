package com.reverse.approval.internal.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ApprovalLineRequest {
    @NotNull(message = "결재 순서가 없을 수가 없습니다.")
    @Min(value = 1, message = "결재 순서는 1 이상이어야 합니다.")
    byte approvalSeq;

    @NotNull(message = "결재자가 없을수는 없습니다.")
    Long approverId;
}
