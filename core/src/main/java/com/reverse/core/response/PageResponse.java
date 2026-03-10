package com.reverse.core.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    /** 1-based 페이징을 위한 응답 객체 생성 (Attendance 모듈 등) */
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        if (size <= 0) {
            throw new IllegalArgumentException("페이지 크기(size)는 1 이상이어야 합니다.");
        }
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean last = page >= totalPages;
        return new PageResponse<>(content, page, size, totalElements, totalPages, last);
    }

    /** 0-based 페이징을 위한 응답 객체 생성 (Approval 모듈 등 기타 환경 지원) */
    public static <T> PageResponse<T> ofZeroBased(
            List<T> content, int page, int size, long totalElements) {
        if (size <= 0) {
            throw new IllegalArgumentException("페이지 크기(size)는 1 이상이어야 합니다.");
        }
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean last = totalPages == 0 || page >= (totalPages - 1);
        return new PageResponse<>(content, page, size, totalElements, totalPages, last);
    }
}
