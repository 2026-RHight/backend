package com.reverse.approval.internal.dto.request;

import jakarta.validation.constraints.NotNull;

public record ApprovalProcessRequest(
        @NotNull(message = "approve 값은 필수입니다.") Boolean approve, String reason) {}
