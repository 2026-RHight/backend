package com.reverse.hr.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeType {
    SYSTEM("시스템 공지"),
    POLICY("정책 변경 공지"),
    HR_ANNOUNCEMENT("인사 발령 공지");

    private final String description;
}
