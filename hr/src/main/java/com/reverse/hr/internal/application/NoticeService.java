package com.reverse.hr.internal.application;

import com.reverse.core.exception.NotFoundException;
import com.reverse.core.response.PageResponse;
import com.reverse.hr.internal.dto.response.NoticeDetailResponseDTO;
import com.reverse.hr.internal.dto.response.NoticeListItemResponseDTO;
import com.reverse.hr.internal.persistence.NoticeMapper;
import com.reverse.hr.internal.persistence.row.NoticeDetailRow;
import com.reverse.hr.internal.persistence.row.NoticeItemRow;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private static final int DEFAULT_LIST_SIZE = 10;
    private static final int MAX_RECENT_SIZE = 8;
    private static final DateTimeFormatter NOTICE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final NoticeMapper noticeMapper;

    public List<NoticeListItemResponseDTO> getRecentNotices(int size) {
        int safeSize = Math.min(MAX_RECENT_SIZE, Math.max(1, size));
        return noticeMapper.findRecentPublishedNotices(safeSize).stream().map(this::toDto).toList();
    }

    public PageResponse<NoticeListItemResponseDTO> getPublishedNotices(int page) {
        int safePage = Math.max(1, page);
        int limit = DEFAULT_LIST_SIZE;

        long offsetLong = (long) (safePage - 1) * limit;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("조회 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;

        long total = noticeMapper.countPublishedNotices();
        List<NoticeListItemResponseDTO> content =
                noticeMapper.findPublishedNotices(limit, offset).stream().map(this::toDto).toList();
        return PageResponse.of(content, safePage, limit, total);
    }

    public NoticeDetailResponseDTO getPublishedNoticeDetail(Long noticeId) {
        NoticeDetailRow row =
                noticeMapper
                        .findPublishedNoticeDetailById(noticeId)
                        .orElseThrow(() -> new NotFoundException("공지사항을 찾을 수 없습니다."));

        String createdDate =
                row.baseDateTime() == null
                        ? "-"
                        : row.baseDateTime().toLocalDate().format(NOTICE_DATE_FORMAT);
        String noticeTypeDescription =
                row.noticeType() == null ? "" : row.noticeType().getDescription();

        return new NoticeDetailResponseDTO(
                row.noticeId(),
                row.title(),
                row.content(),
                createdDate,
                row.authorOrgName(),
                row.authorEmployeeName(),
                row.noticeType(),
                noticeTypeDescription);
    }

    private NoticeListItemResponseDTO toDto(NoticeItemRow row) {
        String createdDate =
                row.baseDateTime() == null
                        ? "-"
                        : row.baseDateTime().toLocalDate().format(NOTICE_DATE_FORMAT);

        String noticeTypeDescription =
                row.noticeType() == null ? "" : row.noticeType().getDescription();

        return new NoticeListItemResponseDTO(
                row.noticeId(),
                row.title(),
                createdDate,
                Boolean.TRUE.equals(row.isPinned()),
                row.authorOrgName(),
                row.authorEmployeeName(),
                row.noticeType(),
                noticeTypeDescription);
    }
}
