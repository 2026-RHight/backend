package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.NoticeType;
import java.time.LocalDateTime;

public record NoticeDetailRow(
        Long noticeId,
        String title,
        String content,
        LocalDateTime baseDateTime,
        String authorOrgName,
        String authorEmployeeName,
        NoticeType noticeType) {}
