package com.reverse.hr.internal.application;

import com.reverse.core.security.FieldCryptoService;
import com.reverse.hr.internal.dto.request.CreateSkillRequestDTO;
import com.reverse.hr.internal.dto.response.CreateSkillResponseDTO;
import com.reverse.hr.internal.dto.response.MyPageResponseDTO;
import com.reverse.hr.internal.persistence.MyPageMapper;
import com.reverse.hr.internal.persistence.param.SkillCreateParam;
import com.reverse.hr.internal.persistence.row.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MyPageMapper myPageMapper;
    private final FieldCryptoService fieldCryptoService;
    private final S3FileService s3FileService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "jpg", "jpeg", "png");
    private static final Set<String> ALLOWED_CONTENT_TYPE = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    public MyPageResponseDTO getMyPage(Long employeeId){
        BasicInfoRow basicInfoRow = myPageMapper.findBasicInfoByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalStateException("기본 정보를 찾을 수 없습니다."));

        String residentPlain = decryptNullable(basicInfoRow.residentNumberEnc());
        String accountPlain = decryptNullable(basicInfoRow.accountNumberEnc());

        String residentMasked = maskResidentNumber(residentPlain);
        String accountMasked = maskAccountNumber(accountPlain);

        HrInfoRow hrInfoRow = myPageMapper.findHrInfoByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalStateException("인사 정보를 찾을 수 없습니다."));

        List<SkillItemRow> skillRows = myPageMapper.findSkillsByEmployeeId(employeeId);
        List<CareerItemRow> careerRows = myPageMapper.findCareersByEmployeeId(employeeId);

        MyPageResponseDTO.BasicInfo basicInfo = new MyPageResponseDTO.BasicInfo(
                basicInfoRow.employeeNum(),
                basicInfoRow.employeeName(),
                basicInfoRow.email(),
                basicInfoRow.phone(),
                basicInfoRow.extensionNum(),
                formatDate(basicInfoRow.birthDate()),
                basicInfoRow.address(),
                residentMasked,
                basicInfoRow.bankName(),
                accountMasked,
                basicInfoRow.profileFileUrl()
        );

        MyPageResponseDTO.HrInfo hrInfo = new MyPageResponseDTO.HrInfo(
                hrInfoRow.orgName(),
                hrInfoRow.positionName(),
                hrInfoRow.rankName(),
                hrInfoRow.jobName(),
                hrInfoRow.employeeState(),
                formatDate(hrInfoRow.hireDate()),
                toTenureText(hrInfoRow.hireDate()),
                hrInfoRow.employType(),
                hrInfoRow.recruitType(),
                hrInfoRow.areaName()
        );

        List<MyPageResponseDTO.SkillItem> skills = skillRows.stream()
                .map(row -> new MyPageResponseDTO.SkillItem(
                        row.category(),
                        row.skillName(),
                        row.acquisitionDate(),
                        row.licenseNumber(),
                        row.hrFileId()
                ))
                .toList();

        List<MyPageResponseDTO.CareerItem> careers = careerRows.stream()
                .map(row -> new MyPageResponseDTO.CareerItem(
                        row.careerId(),
                        row.companyName(),
                        row.orgName(),
                        row.startDate(),
                        row.endDate(),
                        row.hrFileId()
                ))
                .toList();

        return new MyPageResponseDTO(basicInfo, hrInfo, skills, careers);
    }

    @Transactional
    public CreateSkillResponseDTO createSkill(Long employeeId, CreateSkillRequestDTO request, MultipartFile file){
        validateSkillRequest(request);
        validateEvidenceFile(file);
        EvidenceUploadResult uploadResult = uploadEvidenceFile(employeeId, file, "hr/skill");

        try {
            SkillCreateParam param = new SkillCreateParam(
                    null,
                    employeeId,
                    request.category(),
                    request.skillName(),
                    request.acquisitionDate(),
                    request.licenseNumber(),
                    uploadResult.hrFileId()
            );

            int inserted = myPageMapper.insertSkill(param);
            if (inserted != 1) {
                throw new IllegalStateException("역량 정보 저장 중 오류가 발생했습니다.");
            }

            return new CreateSkillResponseDTO(param.getSkillId(), uploadResult.hrFileId());
        } catch (RuntimeException e) {
            deleteQuietly(uploadResult.s3Key());
            throw e;
        }
    }

    private EvidenceUploadResult uploadEvidenceFile(Long employeeId, MultipartFile file, String baseDir){
        S3FileService.UploadResult uploaded = s3FileService.upload(file, baseDir + "/" + employeeId);

        HrFileRow hrFile = new HrFileRow(
                null,
                uploaded.fileUrl(),
                uploaded.originalName()
        );

        try {
            myPageMapper.insertHrFile(hrFile);
            return new EvidenceUploadResult(hrFile.getHrFileId(), uploaded.key());
        } catch (RuntimeException e) {
            deleteQuietly(uploaded.key());
            throw e;
        }
    }

    private void validateSkillRequest(CreateSkillRequestDTO request) {
        if (request.skillName() != null && request.skillName().length() > 255) {
            throw new IllegalArgumentException("자격명은 255자 이하여야 합니다.");
        }
        if (request.acquisitionDate() != null && request.acquisitionDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("취득일은 오늘 이후 날짜로 입력할 수 없습니다.");
        }
    }

    private void validateEvidenceFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("증빙 파일은 필수입니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB 이하여야 합니다.");
        }

        String originalName = file.getOriginalFilename();
        String ext = extractExt(originalName);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPE.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 타입입니다.");
        }
    }

    private void deleteQuietly(String key) {
        try {
            s3FileService.delete(key);
        } catch (RuntimeException ignored) {
            // 보상 삭제 실패는 원본 예외를 우선한다.
        }
    }

    private String extractExt(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    private String toTenureText(LocalDate hireDate) {
        if (hireDate == null) {
            return null;
        }
        Period period = Period.between(hireDate, LocalDate.now());
        int years = Math.max(period.getYears(), 0);
        int months = Math.max(period.getMonths(), 0);
        return years + "년 " + months + "개월";
    }

    private String decryptNullable(String enc) {
        if (enc == null || enc.isBlank()) {
            return null;
        }
        return fieldCryptoService.decrypt(enc);
    }

    private String maskResidentNumber(String residentPlain) {
        if (residentPlain == null || residentPlain.isBlank()) {
            return null;
        }
        String digits = residentPlain.replaceAll("[^0-9]", "");
        if (digits.length() < 7) {
            return "******";
        }
        return digits.substring(0, 6) + "-" + digits.substring(6, 7) + "******";
    }

    private String maskAccountNumber(String accountPlain) {
        if (accountPlain == null || accountPlain.isBlank()) {
            return null;
        }
        String digits = accountPlain.replaceAll("[^0-9]", "");
        if (digits.length() <= 7) {
            return "***";
        }
        String prefix = digits.substring(0, 3);
        String suffix = digits.substring(digits.length() - 4);
        return prefix + "-****-****-" + suffix;
    }

    private record EvidenceUploadResult(Long hrFileId, String s3Key) {
    }
}
