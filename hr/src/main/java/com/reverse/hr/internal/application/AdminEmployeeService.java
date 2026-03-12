package com.reverse.hr.internal.application;

import com.reverse.core.exception.NotFoundException;
import com.reverse.core.response.PageResponse;
import com.reverse.core.security.FieldCryptoService;
import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.HrEventType;
import com.reverse.hr.internal.domain.enums.RecruitType;
import com.reverse.hr.internal.domain.enums.SensitiveFieldType;
import com.reverse.hr.internal.dto.request.AdminEmployeeCreateRequestDTO;
import com.reverse.hr.internal.dto.response.AdminEmployeeCreateResponseDTO;
import com.reverse.hr.internal.dto.response.AdminEmployeeDetailResponseDTO;
import com.reverse.hr.internal.dto.response.AdminEmployeeListItemResponseDTO;
import com.reverse.hr.internal.dto.response.AdminSensitiveValueResponseDTO;
import com.reverse.hr.internal.dto.response.EvidenceFileResponseDTO;
import com.reverse.hr.internal.persistence.AdminEmployeeMapper;
import com.reverse.hr.internal.persistence.MyPageMapper;
import com.reverse.hr.internal.persistence.row.AdminEmployeeDetailRow;
import com.reverse.hr.internal.persistence.row.CareerItemRow;
import com.reverse.hr.internal.persistence.row.HrFileRow;
import com.reverse.hr.internal.persistence.row.SkillItemRow;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEmployeeService {

    private static final String DEFAULT_EVALUATEE_ROLE_CODE = "EVALUATEE";
    private static final String DEFAULT_PROFILE_FILE_URL =
            "https://static.rhight.local/profiles/default.png";
    private static final String DEFAULT_PROFILE_FILE_TITLE = "기본 프로필 이미지";
    private static final String EMPLOYEE_NUM_DATE_PATTERN = "%1$ty%1$tm%1$td";
    private static final int EMPLOYEE_NUM_RETRY_ATTEMPTS = 20;
    private static final int TEMP_PASSWORD_LENGTH = 14;
    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%^&*";

    private final AdminEmployeeMapper adminEmployeeMapper;
    private final MyPageMapper myPageMapper;
    private final FieldCryptoService fieldCryptoService;
    private final ResidentHashService residentHashService;
    private final AccountHashService accountHashService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AdminEmployeeCreateResponseDTO createEmployee(AdminEmployeeCreateRequestDTO request) {
        validateReferenceIds(
                request.orgId(),
                request.jobId(),
                request.positionId(),
                request.rankId(),
                request.areaId());

        Long evaluateeRoleId = adminEmployeeMapper.findRoleIdByCode(DEFAULT_EVALUATEE_ROLE_CODE);
        if (evaluateeRoleId == null) {
            throw new IllegalStateException("기본 권한(EVALUATEE)을 찾을 수 없습니다.");
        }

        Set<Long> roleIds =
                request.roleIds().stream()
                        .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        roleIds.add(evaluateeRoleId);
        validateRoleIds(roleIds);

        String tempPassword = generateTempPassword();
        String encodedPassword = passwordEncoder.encode(tempPassword);

        String accountEnc = fieldCryptoService.encrypt(request.accountNumber());
        String accountHash = accountHashService.hash(request.accountNumber());
        String residentEnc = fieldCryptoService.encrypt(request.residentNumber());
        String residentHash = residentHashService.hash(request.residentNumber());

        adminEmployeeMapper.insertDefaultProfileFile(
                DEFAULT_PROFILE_FILE_URL, DEFAULT_PROFILE_FILE_TITLE);
        Long profileId = adminEmployeeMapper.findLastInsertedHrFileId();
        if (profileId == null || profileId < 1) {
            throw new IllegalStateException("기본 프로필 생성 중 오류가 발생했습니다.");
        }

        String employeeNum = null;
        int insertedEmployee = 0;
        for (int attempt = 1; attempt <= EMPLOYEE_NUM_RETRY_ATTEMPTS; attempt++) {
            employeeNum = generateEmployeeNum(request.hireDate(), attempt - 1);
            try {
                insertedEmployee =
                        adminEmployeeMapper.insertEmployee(
                                employeeNum,
                                request.employeeName(),
                                encodedPassword,
                                request.phone(),
                                request.extensionNum(),
                                request.email(),
                                request.address(),
                                request.birthDate(),
                                request.bankName(),
                                accountEnc,
                                accountHash,
                                residentEnc,
                                residentHash,
                                true,
                                request.employeeState(),
                                request.hireDate(),
                                profileId);
                break;
            } catch (DuplicateKeyException ex) {
                log.warn(
                        "사번 중복으로 재시도합니다. employeeNum={}, attempt={}/{}",
                        employeeNum,
                        attempt,
                        EMPLOYEE_NUM_RETRY_ATTEMPTS);
                if (attempt == EMPLOYEE_NUM_RETRY_ATTEMPTS) {
                    throw new IllegalStateException("사번 생성 충돌이 반복되어 등록에 실패했습니다.", ex);
                }
            }
        }
        if (insertedEmployee != 1) {
            throw new IllegalStateException("사원 기본 정보 저장 중 오류가 발생했습니다.");
        }

        Long employeeId = adminEmployeeMapper.findLastInsertedEmployeeId();
        if (employeeId == null || employeeId < 1) {
            throw new IllegalStateException("사원 ID 생성 중 오류가 발생했습니다.");
        }

        int insertedHrInfo =
                adminEmployeeMapper.insertEmployeeHrInfo(
                        employeeId,
                        request.orgId(),
                        request.hireDate(),
                        request.positionId(),
                        request.rankId(),
                        request.jobId(),
                        request.employType(),
                        request.recruitType(),
                        request.areaId());
        if (insertedHrInfo != 1) {
            throw new IllegalStateException("인사 정보 저장 중 오류가 발생했습니다.");
        }

        for (Long roleId : roleIds) {
            int insertedRole = adminEmployeeMapper.insertEmployeeRole(employeeId, roleId);
            if (insertedRole != 1) {
                throw new IllegalStateException("권한 저장 중 오류가 발생했습니다.");
            }
        }

        int insertedPasswordHistory =
                adminEmployeeMapper.insertPasswordHistory(employeeId, encodedPassword);
        if (insertedPasswordHistory != 1) {
            throw new IllegalStateException("비밀번호 이력 저장 중 오류가 발생했습니다.");
        }

        return new AdminEmployeeCreateResponseDTO(
                employeeId, employeeNum, request.employeeName(), true);
    }

    public PageResponse<AdminEmployeeListItemResponseDTO> getEmployees(
            String keyword,
            Long orgId,
            EmployeeState employeeState,
            EmployType employType,
            int page,
            int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(100, Math.max(1, size));
        int limit = safeSize;

        long offsetLong = (long) (safePage - 1) * safeSize;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("조회 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;

        long total = adminEmployeeMapper.countEmployees(keyword, orgId, employeeState, employType);
        List<AdminEmployeeListItemResponseDTO> content =
                adminEmployeeMapper
                        .findEmployees(keyword, orgId, employeeState, employType, limit, offset)
                        .stream()
                        .map(
                                row ->
                                        new AdminEmployeeListItemResponseDTO(
                                                row.employeeId(),
                                                row.employeeNum(),
                                                row.employeeName(),
                                                row.profileFileUrl(),
                                                row.orgName(),
                                                row.positionName(),
                                                row.jobName(),
                                                row.rankName(),
                                                row.employeeState(),
                                                description(row.employeeState()),
                                                row.hireDate(),
                                                row.employType(),
                                                description(row.employType()),
                                                row.areaName()))
                        .toList();

        return PageResponse.of(content, safePage, safeSize, total);
    }

    public AdminEmployeeDetailResponseDTO getEmployeeDetail(Long employeeId) {
        AdminEmployeeDetailRow row =
                adminEmployeeMapper
                        .findEmployeeDetailById(employeeId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "EMPLOYEE_NOT_FOUND", "사원 정보를 찾을 수 없습니다."));

        String residentMasked = maskResidentNumber(decryptNullable(row.residentNumberEnc()));
        String accountMasked = maskAccountNumber(decryptNullable(row.accountNumberEnc()));
        List<SkillItemRow> skillRows = myPageMapper.findSkillsByEmployeeId(employeeId);
        List<CareerItemRow> careerRows = myPageMapper.findCareersByEmployeeId(employeeId);

        List<AdminEmployeeDetailResponseDTO.SkillItem> skills =
                skillRows.stream()
                        .map(
                                skill ->
                                        new AdminEmployeeDetailResponseDTO.SkillItem(
                                                skill.skillId(),
                                                skill.category(),
                                                skill.skillName(),
                                                skill.acquisitionDate(),
                                                skill.licenseNumber(),
                                                skill.hrFileId()))
                        .toList();

        List<AdminEmployeeDetailResponseDTO.CareerItem> careers =
                careerRows.stream()
                        .map(
                                career ->
                                        new AdminEmployeeDetailResponseDTO.CareerItem(
                                                career.careerId(),
                                                career.companyName(),
                                                career.orgName(),
                                                career.startDate(),
                                                career.endDate(),
                                                career.hrFileId()))
                        .toList();

        List<AdminEmployeeDetailResponseDTO.HrHistoryItem> hrHistories =
                adminEmployeeMapper.findHrEventsByEmployeeId(employeeId).stream()
                        .map(
                                history ->
                                        new AdminEmployeeDetailResponseDTO.HrHistoryItem(
                                                history.hrEventId(),
                                                history.eventType(),
                                                description(history.eventType()),
                                                history.eventTitle(),
                                                history.requestedAt(),
                                                history.approvedAt(),
                                                history.effectiveFrom(),
                                                history.effectiveTo(),
                                                history.reason(),
                                                history.beforeChange(),
                                                history.afterChange()))
                        .toList();

        return new AdminEmployeeDetailResponseDTO(
                row.employeeId(),
                row.employeeNum(),
                row.employeeName(),
                row.profileFileUrl(),
                row.email(),
                row.phone(),
                row.extensionNum(),
                row.address(),
                row.birthDate(),
                row.orgName(),
                row.positionName(),
                row.jobName(),
                row.rankName(),
                row.employeeState(),
                description(row.employeeState()),
                row.hireDate(),
                row.employType(),
                description(row.employType()),
                row.recruitType(),
                description(row.recruitType()),
                row.areaName(),
                row.bankName(),
                residentMasked,
                accountMasked,
                skills,
                careers,
                hrHistories);
    }

    @Transactional
    public AdminSensitiveValueResponseDTO revealSensitiveField(
            Long viewerEmployeeId,
            Long targetEmployeeId,
            SensitiveFieldType fieldType,
            String reason) {
        AdminEmployeeDetailRow row =
                adminEmployeeMapper
                        .findEmployeeDetailById(targetEmployeeId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "EMPLOYEE_NOT_FOUND", "사원 정보를 찾을 수 없습니다."));

        String value =
                switch (fieldType) {
                    case RESIDENT_NUMBER -> decryptNullable(row.residentNumberEnc());
                    case ACCOUNT_NUMBER -> decryptNullable(row.accountNumberEnc());
                };

        int inserted =
                adminEmployeeMapper.insertSensitiveAccessLog(
                        viewerEmployeeId, targetEmployeeId, fieldType, reason);

        if (inserted != 1) {
            throw new IllegalStateException("민감정보 조회 이력 저장 중 오류가 발생했습니다.");
        }

        return new AdminSensitiveValueResponseDTO(targetEmployeeId, fieldType, value);
    }

    public EvidenceFileResponseDTO getEmployeeSkillEvidence(Long employeeId, Long skillId) {
        HrFileRow fileRow =
                myPageMapper
                        .findSkillFileByIdAndEmployeeId(employeeId, skillId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "EVIDENCE_NOT_FOUND", "증빙 파일을 찾을 수 없습니다."));
        return new EvidenceFileResponseDTO(
                fileRow.getHrFileId(), fileRow.getFileTitle(), fileRow.getFileUrl());
    }

    public EvidenceFileResponseDTO getEmployeeCareerEvidence(Long employeeId, Long careerId) {
        HrFileRow fileRow =
                myPageMapper
                        .findCareerFileByIdAndEmployeeId(employeeId, careerId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "EVIDENCE_NOT_FOUND", "증빙 파일을 찾을 수 없습니다."));
        return new EvidenceFileResponseDTO(
                fileRow.getHrFileId(), fileRow.getFileTitle(), fileRow.getFileUrl());
    }

    private String description(EmployeeState value) {
        return value == null ? null : value.getDescription();
    }

    private String description(EmployType value) {
        return value == null ? null : value.getDescription();
    }

    private String description(RecruitType value) {
        return value == null ? null : value.getDescription();
    }

    private String description(HrEventType value) {
        return value == null ? null : value.getDescription();
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

    private void validateReferenceIds(
            Long orgId, Long jobId, Long positionId, Long rankId, Long areaId) {
        if (adminEmployeeMapper.existsOrganization(orgId) != 1) {
            throw new NotFoundException("ORG_NOT_FOUND", "유효한 조직이 아닙니다.");
        }
        if (adminEmployeeMapper.existsJob(jobId) != 1) {
            throw new NotFoundException("JOB_NOT_FOUND", "유효한 직무가 아닙니다.");
        }
        if (adminEmployeeMapper.existsPosition(positionId) != 1) {
            throw new NotFoundException("POSITION_NOT_FOUND", "유효한 직책이 아닙니다.");
        }
        if (adminEmployeeMapper.existsRank(rankId) != 1) {
            throw new NotFoundException("RANK_NOT_FOUND", "유효한 직급이 아닙니다.");
        }
        if (adminEmployeeMapper.existsWorkingArea(areaId) != 1) {
            throw new NotFoundException("AREA_NOT_FOUND", "유효한 근무지가 아닙니다.");
        }
    }

    private void validateRoleIds(Set<Long> roleIds) {
        if (roleIds.isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 권한이 필요합니다.");
        }
        for (Long roleId : roleIds) {
            if (roleId == null || roleId < 1 || adminEmployeeMapper.existsRole(roleId) != 1) {
                throw new NotFoundException("ROLE_NOT_FOUND", "유효하지 않은 권한이 포함되어 있습니다.");
            }
        }
    }

    private String generateEmployeeNum(LocalDate hireDate, int sequenceOffset) {
        String datePrefix = String.format(Locale.KOREA, EMPLOYEE_NUM_DATE_PATTERN, hireDate);
        Integer maxSequence = adminEmployeeMapper.findMaxDailyEmployeeSequence(datePrefix);
        int nextSequence =
                (maxSequence == null ? 1 : maxSequence + 1) + Math.max(0, sequenceOffset);
        if (nextSequence <= 9999) {
            return datePrefix + String.format(Locale.KOREA, "%04d", nextSequence);
        }
        throw new IllegalStateException("사번 생성 한도를 초과했습니다. 관리자에게 문의해주세요.");
    }

    private String generateTempPassword() {
        StringBuilder builder = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            int index = secureRandom.nextInt(TEMP_PASSWORD_CHARS.length());
            builder.append(TEMP_PASSWORD_CHARS.charAt(index));
        }
        return builder.toString();
    }
}
