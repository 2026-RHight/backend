package com.reverse.hr.internal.dto.request;

import com.reverse.hr.internal.domain.enums.NoticeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNoticeRequestDTO(
        @NotBlank(message = "공지 제목은 필수입니다.") @Size(max = 255, message = "공지 제목은 255자 이하여야 합니다.")
                String title,
        @NotBlank(message = "공지 내용은 필수입니다.") String content,
        @NotNull(message = "공지 카테고리는 필수입니다.") NoticeType noticeType) {}
