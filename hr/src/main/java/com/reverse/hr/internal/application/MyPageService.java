package com.reverse.hr.internal.application;

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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public MyPageResponseDTO getMyPage(Long employeeId){
        BasicInfoRow basicInfoRow = myPageMapper.findBasicInfoByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalStateException("기본 정보를 찾을 수 없습니다."));

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
                basicInfoRow.residentNumberMasked(),
                basicInfoRow.bankName(),
                basicInfoRow.accountNumberMasked(),
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
}
