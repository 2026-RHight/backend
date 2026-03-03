package com.reverse.approval.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApprovalStatus {
    TEMP("임시 저장"),
    PENDING("결재 진행 중"),
    DELEGATED("전결"),
    COMPLETE("결재 완료"),
    REJECTED("반려"),
    WITHDRAWN("회수");

    private final String description;

}
