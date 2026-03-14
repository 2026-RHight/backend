package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.NoticeType;
import java.time.LocalDateTime;

public record NoticeItemRow(
        Long noticeId,
        String title,
        NoticeType noticeType,
        LocalDateTime baseDateTime,
        Boolean isPinned,
        String authorOrgName,
        String authorEmployeeName) {}
