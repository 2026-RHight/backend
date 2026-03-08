package com.reverse.approval.internal.web;

import com.reverse.approval.internal.application.ApprovalService;
import com.reverse.approval.internal.dto.request.DraftApproval;
import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
        return (ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success()));
    }

    @Override
    @PostMapping(path = "/temp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<ApiResponse<String>> tempApproval(
            @RequestPart(value = "dto") DraftApproval dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUser user) {
        return (ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success()));
    }
}
