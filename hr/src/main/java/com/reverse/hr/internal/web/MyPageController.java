package com.reverse.hr.internal.web;

import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.CustomUser;
import com.reverse.hr.internal.application.MyPageService;
import com.reverse.hr.internal.dto.request.CreateCareerRequestDTO;
import com.reverse.hr.internal.dto.request.CreateSkillRequestDTO;
import com.reverse.hr.internal.dto.response.CreateCareerResponseDTO;
import com.reverse.hr.internal.dto.response.CreateSkillResponseDTO;
import com.reverse.hr.internal.dto.response.MyPageResponseDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
@SecurityRequirement(name="JWT")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ApiResponse<MyPageResponseDTO> mypage(@AuthenticationPrincipal CustomUser user){
        return ApiResponse.success(myPageService.getMyPage(user.getEmployeeId()));
    }

    @PostMapping(value = "/skills", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CreateSkillResponseDTO> createSkills (@AuthenticationPrincipal CustomUser user,
                                                             @Valid @RequestPart("request") CreateSkillRequestDTO request,
                                                             @RequestPart("file")MultipartFile file){
        return ApiResponse.success(myPageService.createSkill(user.getEmployeeId(), request, file));
    }

    @PostMapping(value = "/careers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CreateCareerResponseDTO> createCareer(
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestPart("request") CreateCareerRequestDTO request,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success(myPageService.createCareer(user.getEmployeeId(), request, file));
    }



}
