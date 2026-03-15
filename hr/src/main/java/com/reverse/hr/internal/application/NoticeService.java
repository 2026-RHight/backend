package com.reverse.hr.internal.application;

import com.reverse.core.exception.NotFoundException;
import com.reverse.core.response.PageResponse;
import com.reverse.hr.internal.domain.enums.NoticeType;
import com.reverse.hr.internal.dto.request.CreateNoticeRequestDTO;
import com.reverse.hr.internal.dto.response.NoticeDetailResponseDTO;
import com.reverse.hr.internal.dto.response.NoticeListItemResponseDTO;
import com.reverse.hr.internal.persistence.NoticeMapper;
import com.reverse.hr.internal.persistence.row.NoticeAuthorRow;
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

    public PageResponse<NoticeListItemResponseDTO> getPublishedNotices(
            NoticeType noticeType, int page) {
        int safePage = Math.max(1, page);
        int limit = DEFAULT_LIST_SIZE;

        long offsetLong = (long) (safePage - 1) * limit;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("조회 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;

        String noticeTypeCode = noticeType == null ? null : noticeType.name();
        long total = noticeMapper.countPublishedNotices(noticeTypeCode);
        List<NoticeListItemResponseDTO> content =
                noticeMapper.findPublishedNotices(noticeTypeCode, limit, offset).stream()
                        .map(this::toDto)
                        .toList();
        return PageResponse.of(content, safePage, limit, total);
    }

    public PageResponse<NoticeListItemResponseDTO> getMyPublishedNotices(
            Long authorEmployeeId, NoticeType noticeType, int page) {
        int safePage = Math.max(1, page);
        int limit = DEFAULT_LIST_SIZE;

        long offsetLong = (long) (safePage - 1) * limit;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("조회 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;

        String noticeTypeCode = noticeType == null ? null : noticeType.name();
        long total = noticeMapper.countPublishedNoticesByAuthor(authorEmployeeId, noticeTypeCode);
        List<NoticeListItemResponseDTO> content =
                noticeMapper
                        .findPublishedNoticesByAuthor(
                                authorEmployeeId, noticeTypeCode, limit, offset)
                        .stream()
                        .map(this::toDto)
                        .toList();
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

    @Transactional
    public void createNotice(Long authorEmployeeId, CreateNoticeRequestDTO request) {
        NoticeAuthorRow authorRow =
                noticeMapper
                        .findNoticeAuthorByEmployeeId(authorEmployeeId)
                        .orElseThrow(() -> new NotFoundException("작성자 정보를 찾을 수 없습니다."));

        int inserted =
                noticeMapper.insertNotice(
                        request.title().trim(),
                        request.content().trim(),
                        request.noticeType().name(),
                        authorRow.employeeId(),
                        authorRow.employeeName(),
                        authorRow.orgName());
        if (inserted != 1) {
            throw new IllegalStateException("공지사항 등록 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updateNoticePinned(Long noticeId, boolean isPinned) {
        int updated = noticeMapper.updateNoticePinned(noticeId, isPinned);
        if (updated != 1) {
            throw new NotFoundException("공지사항을 찾을 수 없습니다.");
        }
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
