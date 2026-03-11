package com.reverse.hr.internal.application;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.reverse.core.exception.NotFoundException;
import com.reverse.core.exception.UnauthorizedException;
import com.reverse.core.security.FieldCryptoService;
import com.reverse.hr.internal.domain.enums.CertificateRequestStatus;
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
import com.reverse.hr.internal.exception.AuthErrorCode;
import com.reverse.hr.internal.persistence.AuthMapper;
import com.reverse.hr.internal.persistence.MyPageMapper;
import com.reverse.hr.internal.persistence.param.CareerCreateParam;
import com.reverse.hr.internal.persistence.param.CertificateRequestCreateParam;
import com.reverse.hr.internal.persistence.param.SkillCreateParam;
import com.reverse.hr.internal.persistence.param.UpdateBasicInfoParam;
import com.reverse.hr.internal.persistence.row.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MyPageMapper myPageMapper;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final FieldCryptoService fieldCryptoService;
    private final S3FileService s3FileService;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter FILE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50MB
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 20;
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "jpg", "jpeg", "png");
    private static final Set<String> ALLOWED_CONTENT_TYPE =
            Set.of("application/pdf", "image/jpeg", "image/png");
    private static final Set<String> ALLOWED_PROFILE_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_PROFILE_CONTENT_TYPE =
            Set.of("image/jpeg", "image/png", "image/webp");
    private static final long CERTIFICATE_DOWNLOAD_URL_EXPIRE_SECONDS = 300L;

    public MyPageHeaderResponseDTO getMyPageHeader(Long employeeId) {
        MyPageHeaderRow row =
                myPageMapper
                        .findMyPageHeaderByEmployeeId(employeeId)
                        .orElseThrow(() -> new IllegalStateException("상단 헤더 정보를 찾을 수 없습니다."));

        return new MyPageHeaderResponseDTO(
                row.employeeName(),
                row.employeeState(),
                toEmployeeStateDescription(row.employeeState()),
                row.orgName(),
                row.jobName(),
                row.positionName(),
                row.email(),
                row.phone(),
                row.extensionNum(),
                row.areaName(),
                row.profileFileUrl());
    }

    public MyPageResponseDTO getMyPage(Long employeeId) {
        BasicInfoRow basicInfoRow =
                myPageMapper
                        .findBasicInfoByEmployeeId(employeeId)
                        .orElseThrow(() -> new IllegalStateException("기본 정보를 찾을 수 없습니다."));

        String residentPlain = decryptNullable(basicInfoRow.residentNumberEnc());
        String accountPlain = decryptNullable(basicInfoRow.accountNumberEnc());

        String residentMasked = maskResidentNumber(residentPlain);
        String accountMasked = maskAccountNumber(accountPlain);

        HrInfoRow hrInfoRow =
                myPageMapper
                        .findHrInfoByEmployeeId(employeeId)
                        .orElseThrow(() -> new IllegalStateException("인사 정보를 찾을 수 없습니다."));

        List<SkillItemRow> skillRows = myPageMapper.findSkillsByEmployeeId(employeeId);
        List<CareerItemRow> careerRows = myPageMapper.findCareersByEmployeeId(employeeId);

        MyPageResponseDTO.BasicInfo basicInfo =
                new MyPageResponseDTO.BasicInfo(
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
                        basicInfoRow.profileFileUrl());

        MyPageResponseDTO.HrInfo hrInfo =
                new MyPageResponseDTO.HrInfo(
                        hrInfoRow.orgName(),
                        hrInfoRow.positionName(),
                        hrInfoRow.rankName(),
                        hrInfoRow.jobName(),
                        hrInfoRow.employeeState(),
                        toEmployeeStateDescription(hrInfoRow.employeeState()),
                        formatDate(hrInfoRow.hireDate()),
                        toTenureText(hrInfoRow.hireDate()),
                        hrInfoRow.employType(),
                        toEmployTypeDescription(hrInfoRow.employType()),
                        hrInfoRow.recruitType(),
                        toRecruitTypeDescription(hrInfoRow.recruitType()),
                        hrInfoRow.areaName());

        List<MyPageResponseDTO.SkillItem> skills =
                skillRows.stream()
                        .map(
                                row ->
                                        new MyPageResponseDTO.SkillItem(
                                                row.skillId(),
                                                row.category(),
                                                row.skillName(),
                                                row.acquisitionDate(),
                                                row.licenseNumber(),
                                                row.hrFileId()))
                        .toList();

        List<MyPageResponseDTO.CareerItem> careers =
                careerRows.stream()
                        .map(
                                row ->
                                        new MyPageResponseDTO.CareerItem(
                                                row.careerId(),
                                                row.companyName(),
                                                row.orgName(),
                                                row.startDate(),
                                                row.endDate(),
                                                row.hrFileId()))
                        .toList();

        return new MyPageResponseDTO(basicInfo, hrInfo, skills, careers);
    }

    @Transactional
    public CreateSkillResponseDTO createSkill(
            Long employeeId, CreateSkillRequestDTO request, MultipartFile file) {
        validateSkillRequest(request);
        validateEvidenceFile(file);
        EvidenceUploadResult uploadResult = uploadEvidenceFile(employeeId, file, "hr/skill");

        try {
            SkillCreateParam param =
                    new SkillCreateParam(
                            null,
                            employeeId,
                            request.category(),
                            request.skillName(),
                            request.acquisitionDate(),
                            request.licenseNumber(),
                            uploadResult.hrFileId());

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

    @Transactional
    public CreateCareerResponseDTO createCareer(
            Long employeeId, CreateCareerRequestDTO request, MultipartFile file) {
        validateCareerRequest(request);
        validateEvidenceFile(file);
        EvidenceUploadResult uploadResult = uploadEvidenceFile(employeeId, file, "hr/career");

        try {
            CareerCreateParam param =
                    new CareerCreateParam(
                            null,
                            employeeId,
                            request.companyName(),
                            request.orgName(),
                            request.startDate(),
                            request.endDate(),
                            uploadResult.hrFileId());

            int inserted = myPageMapper.insertCareer(param);
            if (inserted != 1) {
                throw new IllegalStateException("경력 사항 저장 중 오류가 발생했습니다.");
            }

            return new CreateCareerResponseDTO(param.getCareerId(), uploadResult.hrFileId());
        } catch (RuntimeException e) {
            deleteQuietly(uploadResult.s3Key());
            throw e;
        }
    }

    @Transactional
    public CreateCertificateRequestResponseDTO createCertificateRequest(
            Long employeeId, CreateCertificateRequestDTO request) {
        BasicInfoRow basicInfoRow =
                myPageMapper
                        .findBasicInfoByEmployeeId(employeeId)
                        .orElseThrow(() -> new IllegalStateException("기본 정보를 찾을 수 없습니다."));

        HrInfoRow hrInfoRow =
                myPageMapper
                        .findHrInfoByEmployeeId(employeeId)
                        .orElseThrow(() -> new IllegalStateException("인사 정보를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        String html = buildCertificateHtml(basicInfoRow, hrInfoRow, request, now);
        byte[] pdfBytes = buildPdfBytes(html);
        String fileName =
                "certificate_"
                        + request.certificateType().name().toLowerCase()
                        + "_"
                        + employeeId
                        + "_"
                        + now.format(FILE_DATE_TIME_FORMATTER)
                        + ".pdf";

        S3FileService.UploadResult uploaded =
                s3FileService.uploadBytes(
                        pdfBytes, fileName, "application/pdf", "hr/certificate/" + employeeId);
        registerRollbackDelete(uploaded.key());

        try {
            HrFileRow hrFile = new HrFileRow(null, uploaded.key(), uploaded.fileUrl(), fileName);
            int fileInserted = myPageMapper.insertHrFile(hrFile);
            if (fileInserted != 1 || hrFile.getHrFileId() == null) {
                throw new IllegalStateException("증명서 파일 저장 중 오류가 발생했습니다.");
            }

            CertificateRequestCreateParam param =
                    new CertificateRequestCreateParam(
                            null,
                            employeeId,
                            request.certificateType(),
                            request.purpose(),
                            request.submitTo(),
                            CertificateRequestStatus.ISSUED,
                            now,
                            now,
                            hrFile.getHrFileId(),
                            null);

            int inserted = myPageMapper.insertCertificateRequest(param);
            if (inserted != 1) {
                throw new IllegalStateException("증명서 발급 이력 저장 중 오류가 발생했습니다.");
            }

            return new CreateCertificateRequestResponseDTO(
                    param.getRequestId(),
                    request.certificateType(),
                    CertificateRequestStatus.ISSUED,
                    now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")),
                    hrFile.getHrFileId());
        } catch (RuntimeException e) {
            deleteQuietly(uploaded.key());
            throw e;
        }
    }

    public List<CertificateRequestHistoryResponseDTO> getCertificateRequestHistories(
            Long employeeId) {
        return myPageMapper.findCertificateRequestsByEmployeeId(employeeId).stream()
                .map(
                        row ->
                                new CertificateRequestHistoryResponseDTO(
                                        row.requestId(),
                                        row.certificateType(),
                                        toCertificateName(row.certificateType()),
                                        row.issuedDate(),
                                        row.status(),
                                        toCertificateStatusName(row.status())))
                .toList();
    }

    @Transactional
    public void updateBasicInfo(
            Long employeeId, UpdateBasicInfoRequestDTO request, MultipartFile profileImage) {
        validateUpdateBasicInfoRequest(request);

        int updated =
                myPageMapper.updateBasicInfo(
                        new UpdateBasicInfoParam(
                                employeeId, request.email(), request.phone(), request.address()));

        if (updated != 1) {
            throw new IllegalStateException("기본 정보 수정 중 오류가 발생했습니다.");
        }

        if (profileImage == null || profileImage.isEmpty()) {
            return;
        }

        validateProfileImageFile(profileImage);
        S3FileService.UploadResult uploaded =
                s3FileService.upload(profileImage, "hr/profile/" + employeeId);
        registerRollbackDelete(uploaded.key());

        try {
            HrFileRow hrFile =
                    new HrFileRow(
                            null, uploaded.key(), uploaded.fileUrl(), uploaded.originalName());
            int inserted = myPageMapper.insertHrFile(hrFile);
            if (inserted != 1 || hrFile.getHrFileId() == null) {
                throw new IllegalStateException("프로필 파일 저장 중 오류가 발생했습니다.");
            }

            int profileUpdated = myPageMapper.updateProfileId(employeeId, hrFile.getHrFileId());
            if (profileUpdated != 1) {
                throw new IllegalStateException("프로필 이미지 반영 중 오류가 발생했습니다.");
            }
        } catch (RuntimeException e) {
            deleteQuietly(uploaded.key());
            throw e;
        }
    }

    @Transactional
    public void changeMyPassword(Long employeeId, ChangeMyPasswordRequestDTO request) {
        LoginUserRow user =
                authMapper
                        .findUserByEmployeeId(employeeId)
                        .orElseThrow(
                                () ->
                                        new UnauthorizedException(
                                                AuthErrorCode.AUTH_LOGIN_FAILED,
                                                "인증 정보가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.currentPassword(), user.password())) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_LOGIN_FAILED, "현재 비밀번호가 올바르지 않습니다.");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new UnauthorizedException(
                    AuthErrorCode.INVALID_PASSWORD_CONFIRM, "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(request.newPassword(), user.password())) {
            throw new UnauthorizedException(
                    AuthErrorCode.INVALID_NEW_PASSWORD, "기존 비밀번호와 다른 비밀번호를 입력해주세요.");
        }

        validateNewPasswordPolicy(request.newPassword());
        String encodedNewPassword = passwordEncoder.encode(request.newPassword());

        int updated =
                authMapper.updatePasswordAndInitialState(employeeId, encodedNewPassword, false);
        int inserted = authMapper.insertPasswordHistory(employeeId, encodedNewPassword);

        if (updated != 1 || inserted != 1) {
            throw new IllegalStateException("비밀번호 변경 처리 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void deleteSkill(Long employeeId, Long skillId) {
        HrFileRow fileRow =
                myPageMapper
                        .findSkillFileByIdAndEmployeeId(employeeId, skillId)
                        .orElseThrow(() -> new IllegalStateException("삭제할 역량 정보를 찾을 수 없습니다."));

        int deleted = myPageMapper.deleteSkillByIdAndEmployeeId(employeeId, skillId);
        if (deleted != 1) {
            throw new IllegalStateException("역량 정보 삭제 중 오류가 발생했습니다.");
        }

        deleteHrFileAndS3IfUnreferenced(fileRow);
    }

    @Transactional
    public void deleteCareer(Long employeeId, Long careerId) {
        HrFileRow fileRow =
                myPageMapper
                        .findCareerFileByIdAndEmployeeId(employeeId, careerId)
                        .orElseThrow(() -> new IllegalStateException("삭제할 경력 정보를 찾을 수 없습니다."));

        int deleted = myPageMapper.deleteCareerByIdAndEmployeeId(employeeId, careerId);
        if (deleted != 1) {
            throw new IllegalStateException("경력 사항 삭제 중 오류가 발생했습니다.");
        }

        deleteHrFileAndS3IfUnreferenced(fileRow);
    }

    public EvidenceFileResponseDTO getSkillEvidenceFile(Long employeeId, Long skillId) {
        HrFileRow fileRow =
                myPageMapper
                        .findSkillFileByIdAndEmployeeId(employeeId, skillId)
                        .orElseThrow(() -> new IllegalStateException("증빙 파일을 찾을 수 없습니다."));

        return new EvidenceFileResponseDTO(
                fileRow.getHrFileId(), fileRow.getFileTitle(), fileRow.getFileUrl());
    }

    public EvidenceFileResponseDTO getCareerEvidenceFile(Long employeeId, Long careerId) {
        HrFileRow fileRow =
                myPageMapper
                        .findCareerFileByIdAndEmployeeId(employeeId, careerId)
                        .orElseThrow(() -> new IllegalStateException("증빙 파일을 찾을 수 없습니다."));

        return new EvidenceFileResponseDTO(
                fileRow.getHrFileId(), fileRow.getFileTitle(), fileRow.getFileUrl());
    }

    public String getCertificateDownloadUrl(Long employeeId, Long requestId) {
        HrFileRow fileRow =
                myPageMapper
                        .findCertificateFileByRequestIdAndEmployeeId(employeeId, requestId)
                        .orElseThrow(() -> new NotFoundException("증명서 파일을 찾을 수 없습니다."));
        if (fileRow.getFileKey() == null || fileRow.getFileKey().isBlank()) {
            throw new NotFoundException("증명서 파일을 찾을 수 없습니다.");
        }
        return s3FileService.generatePresignedUrl(
                fileRow.getFileKey(), CERTIFICATE_DOWNLOAD_URL_EXPIRE_SECONDS);
    }

    private EvidenceUploadResult uploadEvidenceFile(
            Long employeeId, MultipartFile file, String baseDir) {
        S3FileService.UploadResult uploaded =
                s3FileService.upload(file, baseDir + "/" + employeeId);
        registerRollbackDelete(uploaded.key());

        HrFileRow hrFile =
                new HrFileRow(null, uploaded.key(), uploaded.fileUrl(), uploaded.originalName());

        try {
            int inserted = myPageMapper.insertHrFile(hrFile);
            if (inserted != 1 || hrFile.getHrFileId() == null) {
                throw new IllegalStateException("증빙 파일 메타데이터 저장 중 오류가 발생했습니다.");
            }
            return new EvidenceUploadResult(hrFile.getHrFileId(), uploaded.key());
        } catch (RuntimeException e) {
            deleteQuietly(uploaded.key());
            throw e;
        }
    }

    private void registerRollbackDelete(String s3Key) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            deleteQuietly(s3Key);
                        }
                    }
                });
    }

    private void validateSkillRequest(CreateSkillRequestDTO request) {
        if (request.skillName() != null && request.skillName().length() > 255) {
            throw new IllegalArgumentException("자격명은 255자 이하여야 합니다.");
        }
        if (request.acquisitionDate() != null
                && request.acquisitionDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("취득일은 오늘 이후 날짜로 입력할 수 없습니다.");
        }
    }

    private void validateCareerRequest(CreateCareerRequestDTO request) {
        if (request.companyName() != null && request.companyName().length() > 255) {
            throw new IllegalArgumentException("회사명은 255자 이하여야 합니다.");
        }

        if (request.orgName() != null && request.orgName().length() > 255) {
            throw new IllegalArgumentException("직무/소속은 255자 이하여야 합니다.");
        }

        if (request.startDate() == null) {
            throw new IllegalArgumentException("시작일은 필수입니다.");
        }

        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    private void validateUpdateBasicInfoRequest(UpdateBasicInfoRequestDTO request) {
        if (request.email() != null && request.email().length() > 100) {
            throw new IllegalArgumentException("이메일은 100자 이하여야 합니다.");
        }
        if (request.phone() != null && request.phone().length() > 50) {
            throw new IllegalArgumentException("연락처는 50자 이하여야 합니다.");
        }
        if (request.address() != null && request.address().length() > 255) {
            throw new IllegalArgumentException("주소는 255자 이하여야 합니다.");
        }
    }

    private void validateNewPasswordPolicy(String newPassword) {
        if (newPassword == null
                || newPassword.length() < MIN_PASSWORD_LENGTH
                || newPassword.length() > MAX_PASSWORD_LENGTH) {
            throw new UnauthorizedException(
                    AuthErrorCode.INVALID_NEW_PASSWORD, "비밀번호는 8자 이상 20자 이하여야 합니다.");
        }

        boolean hasUpper = newPassword.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = newPassword.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = newPassword.chars().anyMatch(Character::isDigit);
        boolean hasSpecial =
                newPassword.chars().anyMatch(ch -> "!@#$%^&*()-_=+[]{}?".indexOf(ch) >= 0);

        if (!(hasUpper && hasLower && hasDigit && hasSpecial)) {
            throw new UnauthorizedException(
                    AuthErrorCode.INVALID_NEW_PASSWORD,
                    "비밀번호는 영문 대/소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.");
        }
    }

    private void validateEvidenceFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("증빙 파일은 필수입니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 50MB 이하여야 합니다.");
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

    private void validateProfileImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("프로필 이미지는 비어 있을 수 없습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB 이하여야 합니다.");
        }

        String originalName = file.getOriginalFilename();
        String ext = extractExt(originalName);
        if (!ALLOWED_PROFILE_EXT.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않는 프로필 이미지 확장자입니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null
                || !ALLOWED_PROFILE_CONTENT_TYPE.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 프로필 이미지 타입입니다.");
        }
    }

    private void deleteQuietly(String key) {
        try {
            s3FileService.delete(key);
        } catch (RuntimeException ignored) {
            // 보상 삭제 실패는 원본 예외를 우선한다.
        }
    }

    private void deleteHrFileAndS3IfUnreferenced(HrFileRow fileRow) {
        if (fileRow == null || fileRow.getHrFileId() == null) {
            return;
        }

        int refCount = myPageMapper.countHrFileReferences(fileRow.getHrFileId());
        if (refCount > 0) {
            return;
        }

        int deletedHrFile = myPageMapper.deleteHrFileById(fileRow.getHrFileId());
        if (deletedHrFile != 1) {
            throw new IllegalStateException("파일 메타 삭제 중 오류가 발생했습니다.");
        }

        String fileKey = fileRow.getFileKey();
        if (fileKey == null || fileKey.isBlank()) {
            return;
        }
        if (org.springframework.transaction.support.TransactionSynchronizationManager
                .isActualTransactionActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager
                    .registerSynchronization(
                            new org.springframework.transaction.support
                                    .TransactionSynchronization() {
                                @Override
                                public void afterCommit() {
                                    s3FileService.delete(fileKey);
                                }
                            });
        } else {
            s3FileService.delete(fileKey);
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

    private String toEmployeeStateDescription(
            com.reverse.hr.internal.domain.enums.EmployeeState employeeState) {
        if (employeeState == null) {
            return null;
        }
        return employeeState.getDescription();
    }

    private String toEmployTypeDescription(
            com.reverse.hr.internal.domain.enums.EmployType employType) {
        if (employType == null) {
            return null;
        }
        return employType.getDescription();
    }

    private String toRecruitTypeDescription(
            com.reverse.hr.internal.domain.enums.RecruitType recruitType) {
        if (recruitType == null) {
            return null;
        }
        return recruitType.getDescription();
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

    private String buildCertificateHtml(
            BasicInfoRow basicInfoRow,
            HrInfoRow hrInfoRow,
            CreateCertificateRequestDTO request,
            LocalDateTime issuedAt) {
        String templatePath = "templates/certificate/employment-ko.html";

        String residentMasked = null;
        if (basicInfoRow.residentNumberEnc() != null
                && !basicInfoRow.residentNumberEnc().isBlank()) {
            residentMasked =
                    maskResidentNumber(
                            fieldCryptoService.decrypt(basicInfoRow.residentNumberEnc()));
        }
        if (residentMasked == null || residentMasked.isBlank()) {
            residentMasked = "-";
        }

        String html = readTemplate(templatePath);
        return html.replace("${name}", htmlText(basicInfoRow.employeeName()))
                .replace("${employeeNum}", htmlText(basicInfoRow.employeeNum()))
                .replace("${residentNumberMasked}", htmlText(residentMasked))
                .replace("${address}", htmlText(basicInfoRow.address()))
                .replace("${orgName}", htmlText(hrInfoRow.orgName()))
                .replace("${rankName}", htmlText(hrInfoRow.rankName()))
                .replace("${jobName}", htmlText(hrInfoRow.jobName()))
                .replace("${positionName}", htmlText(hrInfoRow.positionName()))
                .replace("${hireDate}", htmlText(formatDate(hrInfoRow.hireDate())))
                .replace(
                        "${issuedDateKo}",
                        htmlText(issuedAt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))))
                .replace("${submitTo}", htmlText(request.submitTo()))
                .replace("${purpose}", htmlText(request.purpose()))
                .replace(
                        "${employmentPeriodKo}",
                        htmlText(formatEmploymentPeriodKo(hrInfoRow.hireDate(), LocalDate.now())));
    }

    private byte[] buildPdfBytes(String html) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            registerPdfFonts(builder);
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("증명서 PDF 생성 중 오류가 발생했습니다.", e);
        }
    }

    private void registerPdfFonts(PdfRendererBuilder builder) {
        ClassPathResource resource = new ClassPathResource("fonts/NotoSansKR-Regular.ttf");
        if (!resource.exists()) {
            throw new IllegalStateException(
                    "한글 PDF 폰트를 찾을 수 없습니다. "
                            + "hr/src/main/resources/fonts/NotoSansKR-Regular.ttf 파일을 확인해주세요.");
        }

        builder.useFont(
                () -> {
                    try {
                        return resource.getInputStream();
                    } catch (IOException e) {
                        throw new IllegalStateException("폰트 파일 로드 실패", e);
                    }
                },
                "NotoSansKR");
    }

    private String readTemplate(String classpathPath) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathPath);
            try (var inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("증명서 템플릿을 읽을 수 없습니다: " + classpathPath, e);
        }
    }

    private String formatEmploymentPeriodKo(LocalDate from, LocalDate to) {
        if (from == null) {
            return "-";
        }
        LocalDate end = to == null ? LocalDate.now() : to;
        return from.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                + " ~ "
                + end.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
    }

    private String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }

    private String htmlText(String value) {
        return HtmlUtils.htmlEscape(valueOrDash(value));
    }

    private String toCertificateName(String type) {
        return switch (type) {
            case "EMPLOYMENT_KO" -> "재직 증명서";
            default -> type;
        };
    }

    private String toCertificateStatusName(String status) {
        return switch (status) {
            case "ISSUED" -> "발급 완료";
            case "FAILED" -> "발급 실패";
            default -> status;
        };
    }

    private record EvidenceUploadResult(Long hrFileId, String s3Key) {}
}
