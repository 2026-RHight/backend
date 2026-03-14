package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.NoticeType;

public record NoticeDetailResponseDTO(
        Long noticeId,
        String title,
        String content,
        String createdDate,
        String authorOrgName,
        String authorEmployeeName,
        NoticeType noticeType,
        String noticeTypeDescription) {}
