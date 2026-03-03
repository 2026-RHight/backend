package com.reverse.approval.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ApprovalLineRequest {
    @NotBlank(message = "결재 순서가 없을 수가 없습니다.")
    byte approvalSeq;

    @NotNull(message = "결재자가 없을수는 없습니다.")
    Long approverId;

}
