package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.NoticeType;

public record NoticeListItemResponseDTO(
        Long noticeId,
        String title,
        String createdDate,
        Boolean isPinned,
        String authorOrgName,
        String authorEmployeeName,
        NoticeType noticeType,
        String noticeTypeDescription) {}
