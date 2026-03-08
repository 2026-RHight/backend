package com.reverse.approval.internal.web;

import com.reverse.approval.internal.dto.request.DraftApproval;
import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ApprovalResource {

    @Operation(summary = "기안 상신 API")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            encoding = @Encoding(name="dto", contentType = "application/json") // application/json으로 안가서 따로 설정해준것.
    ))
    public ResponseEntity<ApiResponse<String>> draftApproval(@RequestPart(value = "dto") DraftApproval dto,
                                                             @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                             @AuthenticationPrincipal CustomUser user);


    @Operation(summary = "기안 임시저장 API")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            encoding = @Encoding(name="dto", contentType = "application/json") // application/json으로 안가서 따로 설정해준것.
    ))
    public ResponseEntity<ApiResponse<String>> tempApproval(@RequestPart(value = "dto") DraftApproval dto,
                                                            @RequestPart(value = "files", required = false)List<MultipartFile> files,
                                                            @AuthenticationPrincipal CustomUser user);

    @Operation(summary = "첨부파일 다운로드 API")
    @SecurityRequirement(name = "JWT")
    ResponseEntity<byte[]> downloadAttachment(@PathVariable("approvalId") Long approvalId,
                                              @PathVariable("fileId") Long fileId);
}
