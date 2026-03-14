package com.reverse.hr.internal.persistence;

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
            @Param("limit") int limit, @Param("offset") int offset);

    long countPublishedNotices();

    Optional<NoticeDetailRow> findPublishedNoticeDetailById(@Param("noticeId") Long noticeId);
}
