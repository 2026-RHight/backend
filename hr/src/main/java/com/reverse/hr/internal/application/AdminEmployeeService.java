package com.reverse.hr.internal.application;

import com.reverse.core.exception.NotFoundException;
import com.reverse.core.response.PageResponse;
import com.reverse.core.security.FieldCryptoService;
import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.RecruitType;
import com.reverse.hr.internal.domain.enums.SensitiveFieldType;
import com.reverse.hr.internal.dto.response.AdminEmployeeDetailResponseDTO;
import com.reverse.hr.internal.dto.response.AdminEmployeeListItemResponseDTO;
import com.reverse.hr.internal.dto.response.AdminSensitiveValueResponseDTO;
import com.reverse.hr.internal.persistence.AdminEmployeeMapper;
import com.reverse.hr.internal.persistence.row.AdminEmployeeDetailRow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEmployeeService {

    private final AdminEmployeeMapper adminEmployeeMapper;
    private final FieldCryptoService fieldCryptoService;

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
                accountMasked);
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

    private String description(EmployeeState value) {
        return value == null ? null : value.getDescription();
    }

    private String description(EmployType value) {
        return value == null ? null : value.getDescription();
    }

    private String description(RecruitType value) {
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
}
