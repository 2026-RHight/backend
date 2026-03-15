package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.persistence.row.NoticeAuthorRow;
import com.reverse.hr.internal.persistence.row.NoticeDetailRow;
import com.reverse.hr.internal.persistence.row.NoticeItemRow;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NoticeMapper {

    List<NoticeItemRow> findRecentPublishedNotices(@Param("limit") int limit);

    List<NoticeItemRow> findPublishedNotices(
            @Param("noticeType") String noticeType,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countPublishedNotices(@Param("noticeType") String noticeType);

    Optional<NoticeDetailRow> findPublishedNoticeDetailById(@Param("noticeId") Long noticeId);

    List<NoticeItemRow> findPublishedNoticesByAuthor(
            @Param("authorEmployeeId") Long authorEmployeeId,
            @Param("noticeType") String noticeType,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countPublishedNoticesByAuthor(
            @Param("authorEmployeeId") Long authorEmployeeId,
            @Param("noticeType") String noticeType);

    Optional<NoticeAuthorRow> findNoticeAuthorByEmployeeId(@Param("employeeId") Long employeeId);

    int insertNotice(
            @Param("title") String title,
            @Param("content") String content,
            @Param("noticeType") String noticeType,
            @Param("authorEmployeeId") Long authorEmployeeId,
            @Param("authorEmployeeName") String authorEmployeeName,
            @Param("authorOrgName") String authorOrgName);

    int updateNoticePinned(@Param("noticeId") Long noticeId, @Param("isPinned") boolean isPinned);
}
