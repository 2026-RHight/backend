package com.reverse.hr.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.response.PageResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.hr.internal.application.NoticeService;
import com.reverse.hr.internal.domain.enums.NoticeType;
import com.reverse.hr.internal.dto.request.CreateNoticeRequestDTO;
import com.reverse.hr.internal.dto.response.NoticeDetailResponseDTO;
import com.reverse.hr.internal.dto.response.NoticeListItemResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Validated
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
            @RequestParam(required = false) NoticeType noticeType,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        return ApiResponse.success(noticeService.getPublishedNotices(noticeType, page));
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지사항 상세 조회")
    public ApiResponse<NoticeDetailResponseDTO> getNoticeDetail(@PathVariable Long noticeId) {
        return ApiResponse.success(noticeService.getPublishedNoticeDetail(noticeId));
    }

    @PostMapping("/admin")
    @Operation(summary = "관리자 공지사항 등록")
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER','HR_ADMIN_BASIC','HR_ADMIN_PAYROLL')")
    public ApiResponse<Void> createNotice(
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestBody CreateNoticeRequestDTO request) {
        noticeService.createNotice(user.getEmployeeId(), request);
        return ApiResponse.success(null);
    }

    @GetMapping("/admin/mine")
    @Operation(summary = "관리자 내가 작성한 공지 목록 조회")
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER','HR_ADMIN_BASIC','HR_ADMIN_PAYROLL')")
    public ApiResponse<PageResponse<NoticeListItemResponseDTO>> getMyNotices(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) NoticeType noticeType,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        return ApiResponse.success(
                noticeService.getMyPublishedNotices(user.getEmployeeId(), noticeType, page));
    }

    @PatchMapping("/admin/{noticeId}/pin")
    @Operation(summary = "관리자 공지사항 고정/해제")
    @PreAuthorize("hasAnyRole('HR_ADMIN_MASTER','HR_ADMIN_BASIC','HR_ADMIN_PAYROLL')")
    public ApiResponse<Void> updateNoticePin(
            @PathVariable Long noticeId, @RequestParam boolean pinned) {
        noticeService.updateNoticePinned(noticeId, pinned);
        return ApiResponse.success();
    }
}
