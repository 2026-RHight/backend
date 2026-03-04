package com.reverse.hr.internal.application;

import com.reverse.core.security.FieldCryptoService;
import com.reverse.hr.internal.application.dto.response.MyPageResponseDTO;
import com.reverse.hr.internal.persistence.MyPageMapper;
import com.reverse.hr.internal.persistence.row.BasicInfoRow;
import com.reverse.hr.internal.persistence.row.CareerItemRow;
import com.reverse.hr.internal.persistence.row.HrInfoRow;
import com.reverse.hr.internal.persistence.row.SkillItemRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MyPageMapper myPageMapper;
    private final FieldCryptoService fieldCryptoService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

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
}
