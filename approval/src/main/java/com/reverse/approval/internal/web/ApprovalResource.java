package com.reverse.approval.internal.web;

import com.reverse.approval.internal.dto.request.ApprovalProcessRequest;
import com.reverse.approval.internal.dto.request.DraftApproval;
import com.reverse.approval.internal.dto.response.ApprovalBoxPageResponse;
import com.reverse.approval.internal.dto.response.ApprovalDetailResponse;
import com.reverse.approval.internal.dto.response.ApprovalProgressOverviewResponse;
import com.reverse.approval.internal.dto.response.ApprovalProgressPageResponse;
import com.reverse.approval.internal.dto.response.ApprovalReviewPageResponse;
import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public interface ApprovalResource {

    @Operation(summary = "기안 상신 API")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content =
                    @Content(
                            encoding =
                                    @Encoding(
                                            name = "dto",
                                            contentType =
                                                    "application/json") // application/json으로 안가서 따로
                            // 설정해준것.
                            ))
    public ResponseEntity<ApiResponse<String>> draftApproval(
            @RequestPart(value = "dto") DraftApproval dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUser user);

    @Operation(summary = "기안 임시저장 API")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content =
                    @Content(
                            encoding =
                                    @Encoding(
                                            name = "dto",
                                            contentType =
                                                    "application/json") // application/json으로 안가서 따로
                            // 설정해준것.
                            ))
    public ResponseEntity<ApiResponse<String>> tempApproval(
            @RequestPart(value = "dto") DraftApproval dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUser user);

    @Operation(summary = "첨부파일 다운로드 API")
    @SecurityRequirement(name = "JWT")
    ResponseEntity<byte[]> downloadAttachment(
            @PathVariable("approvalId") Long approvalId, @PathVariable("fileId") Long fileId);

    @Operation(summary = "결재 처리 API")
    @SecurityRequirement(name = "JWT")
    ResponseEntity<ApiResponse<String>> processApproval(
            @PathVariable("approvalId") Long approvalId,
            @RequestBody ApprovalProcessRequest request,
            @AuthenticationPrincipal CustomUser user);

    @Operation(summary = "기안 상세 조회 API")
    @SecurityRequirement(name = "JWT")
    ResponseEntity<ApiResponse<ApprovalDetailResponse>> getApprovalDetail(
            @PathVariable("approvalId") Long approvalId, @AuthenticationPrincipal CustomUser user);

    @Operation(summary = "기안 읽음 처리 API")
    @SecurityRequirement(name = "JWT")
    ResponseEntity<ApiResponse<String>> markApprovalAsRead(
            @PathVariable("approvalId") Long approvalId, @AuthenticationPrincipal CustomUser user);

    @Operation(summary = "문서함 조회 API")
    @SecurityRequirement(name = "JWT")
    ResponseEntity<ApiResponse<ApprovalBoxPageResponse>> getApprovalBoxes(
            @RequestParam(value = "boxType", defaultValue = "ALL") String boxType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUser user);

    @Operation(summary = "전자결재 현황 진입 API")
    @SecurityRequirement(name = "JWT")
    ResponseEntity<ApiResponse<ApprovalProgressOverviewResponse>> getApprovalProgressOverview(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUser user);

    @Operation(summary = "전자결재 현황 검색 API")
    @SecurityRequirement(name = "JWT")
    ResponseEntity<ApiResponse<ApprovalProgressPageResponse>> searchApprovalProgress(
            @RequestParam(value = "tabType", defaultValue = "ALL") String tabType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUser user);

    @Operation(summary = "전자결재 검토 목록 API")
    @SecurityRequirement(name = "JWT")
    ResponseEntity<ApiResponse<ApprovalReviewPageResponse>> getApprovalReviews(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUser user);
}
