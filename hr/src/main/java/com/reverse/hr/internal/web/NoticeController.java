package com.reverse.hr.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.response.PageResponse;
import com.reverse.hr.internal.application.NoticeService;
import com.reverse.hr.internal.dto.response.NoticeDetailResponseDTO;
import com.reverse.hr.internal.dto.response.NoticeListItemResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/recent")
    @Operation(summary = "메인 대시보드 공지사항 최신 목록")
    public ApiResponse<List<NoticeListItemResponseDTO>> getRecentNotices(
            @RequestParam(defaultValue = "8") @Min(1) int size) {
        return ApiResponse.success(noticeService.getRecentNotices(size));
    }

    @GetMapping
    @Operation(summary = "공지사항 전체 목록 조회")
    public ApiResponse<PageResponse<NoticeListItemResponseDTO>> getNotices(
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        return ApiResponse.success(noticeService.getPublishedNotices(page));
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지사항 상세 조회")
    public ApiResponse<NoticeDetailResponseDTO> getNoticeDetail(@PathVariable Long noticeId) {
        return ApiResponse.success(noticeService.getPublishedNoticeDetail(noticeId));
    }
}
