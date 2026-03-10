package com.reverse.hr.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.hr.internal.application.MyPageService;
import com.reverse.hr.internal.dto.request.ChangeMyPasswordRequestDTO;
import com.reverse.hr.internal.dto.request.CreateCareerRequestDTO;
import com.reverse.hr.internal.dto.request.CreateCertificateRequestDTO;
import com.reverse.hr.internal.dto.request.CreateSkillRequestDTO;
import com.reverse.hr.internal.dto.request.UpdateBasicInfoRequestDTO;
import com.reverse.hr.internal.dto.response.CertificateRequestHistoryResponseDTO;
import com.reverse.hr.internal.dto.response.CreateCareerResponseDTO;
import com.reverse.hr.internal.dto.response.CreateCertificateRequestResponseDTO;
import com.reverse.hr.internal.dto.response.CreateSkillResponseDTO;
import com.reverse.hr.internal.dto.response.EvidenceFileResponseDTO;
import com.reverse.hr.internal.dto.response.MyPageHeaderResponseDTO;
import com.reverse.hr.internal.dto.response.MyPageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/header")
    @Operation(summary = "마이페이지 헤더")
    public ApiResponse<MyPageHeaderResponseDTO> mypageHeader(
            @AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(myPageService.getMyPageHeader(user.getEmployeeId()));
    }

    @GetMapping
    @Operation(summary = "마이페이지 전체 조회")
    public ApiResponse<MyPageResponseDTO> mypage(@AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(myPageService.getMyPage(user.getEmployeeId()));
    }

    @PatchMapping("/password")
    @Operation(summary = "내 비밀번호 변경")
    public ApiResponse<Void> changeMyPassword(
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestBody ChangeMyPasswordRequestDTO request) {
        myPageService.changeMyPassword(user.getEmployeeId(), request);
        return ApiResponse.success();
    }

    @PatchMapping(value = "/basic-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "기본 정보 수정")
    public ApiResponse<Void> updateBasicInfo(
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestPart("request") UpdateBasicInfoRequestDTO request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        myPageService.updateBasicInfo(user.getEmployeeId(), request, profileImage);
        return ApiResponse.success();
    }

    @PostMapping(value = "/skills", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "역량 정보 추가")
    public ApiResponse<CreateSkillResponseDTO> createSkills(
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestPart("request") CreateSkillRequestDTO request,
            @RequestPart("file") MultipartFile file) {
        return ApiResponse.success(myPageService.createSkill(user.getEmployeeId(), request, file));
    }

    @PostMapping(value = "/careers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "경력 사항 추가")
    public ApiResponse<CreateCareerResponseDTO> createCareer(
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestPart("request") CreateCareerRequestDTO request,
            @RequestPart("file") MultipartFile file) {
        return ApiResponse.success(myPageService.createCareer(user.getEmployeeId(), request, file));
    }

    @PostMapping("/certificates/requests")
    @Operation(summary = "증명서 발급 요청")
    public ApiResponse<CreateCertificateRequestResponseDTO> createCertificateRequest(
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestBody CreateCertificateRequestDTO request) {
        return ApiResponse.success(
                myPageService.createCertificateRequest(user.getEmployeeId(), request));
    }

    @GetMapping("/certificates/requests")
    @Operation(summary = "증명서 발급 이력 조회")
    public ApiResponse<List<CertificateRequestHistoryResponseDTO>> getCertificateRequestHistories(
            @AuthenticationPrincipal CustomUser user) {
        return ApiResponse.success(
                myPageService.getCertificateRequestHistories(user.getEmployeeId()));
    }

    @DeleteMapping("/skills/{skillId}")
    @Operation(summary = "역량 정보 삭제")
    public ApiResponse<Void> deleteSkill(
            @AuthenticationPrincipal CustomUser user, @PathVariable Long skillId) {
        myPageService.deleteSkill(user.getEmployeeId(), skillId);
        return ApiResponse.success();
    }

    @DeleteMapping("/careers/{careerId}")
    @Operation(summary = "경력 사항 삭제")
    public ApiResponse<Void> deleteCareer(
            @AuthenticationPrincipal CustomUser user, @PathVariable Long careerId) {
        myPageService.deleteCareer(user.getEmployeeId(), careerId);
        return ApiResponse.success();
    }

    @GetMapping("/skills/{skillId}/evidence")
    @Operation(summary = "역량 증빙 파일 조회")
    public ApiResponse<EvidenceFileResponseDTO> getSkillEvidenceFile(
            @AuthenticationPrincipal CustomUser user, @PathVariable Long skillId) {
        return ApiResponse.success(
                myPageService.getSkillEvidenceFile(user.getEmployeeId(), skillId));
    }

    @GetMapping("/careers/{careerId}/evidence")
    @Operation(summary = "경력 증빙 파일 조회")
    public ApiResponse<EvidenceFileResponseDTO> getCareerEvidenceFile(
            @AuthenticationPrincipal CustomUser user, @PathVariable Long careerId) {
        return ApiResponse.success(
                myPageService.getCareerEvidenceFile(user.getEmployeeId(), careerId));
    }

    @GetMapping("/certificates/{requestId}/download")
    @Operation(summary = "증명서 파일 다운로드")
    public ResponseEntity<Void> downloadCertificate(
            @AuthenticationPrincipal CustomUser user, @PathVariable Long requestId) {
        String fileUrl = myPageService.getCertificateDownloadUrl(user.getEmployeeId(), requestId);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(fileUrl)).build();
    }
}
