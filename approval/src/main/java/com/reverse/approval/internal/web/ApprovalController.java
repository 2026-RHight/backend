package com.reverse.approval.internal.web;

import com.reverse.approval.internal.application.ApprovalService;
import com.reverse.approval.internal.domain.enums.ApprovalStatus;
import com.reverse.approval.internal.dto.request.DraftApproval;
import com.reverse.approval.internal.dto.response.DownloadedApprovalFile;
import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/approval")
public class ApprovalController implements ApprovalResource {

    private final ApprovalService approvalService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<String>> draftApproval(
            @RequestPart(value = "dto") DraftApproval dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                approvalService.draftApproval(
                                        dto, files, user.getEmployeeId(), ApprovalStatus.PENDING)));
    }

    @Override
    @PostMapping(path = "/temp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<String>> tempApproval(
            @RequestPart(value = "dto") DraftApproval dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                approvalService.draftApproval(
                                        dto, files, user.getEmployeeId(), ApprovalStatus.TEMP)));
    }

    @Override
    @GetMapping("/{approvalId}/attachments/{fileId}/download")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable("approvalId") Long approvalId, @PathVariable("fileId") Long fileId) {
        DownloadedApprovalFile downloaded = approvalService.downloadAttachment(approvalId, fileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(downloaded.originalName(), StandardCharsets.UTF_8)
                        .build());

        return new ResponseEntity<>(downloaded.content(), headers, HttpStatus.OK);
    }

    @Operation(summary = "기안 취소 API")
    @DeleteMapping(path = "/{approvalId}")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<?> deleteApproval(
            @PathVariable("approvalId") Long approvalId, @AuthenticationPrincipal CustomUser user) {

        approvalService.deleteApproval(approvalId, user.getEmployeeId());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
