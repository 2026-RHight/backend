package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.RecruitType;
import com.reverse.hr.internal.domain.enums.SkillCategory;

import java.util.List;

public record OrganizationMemberDetailResponseDTO(
        PersonalInfo personalInfo,
        HrInfo hrInfo,
        List<SkillItem> skills,
        List<CareerItem> careers
) {
    public record PersonalInfo(
            String employeeName,
            String email,
            String phone,
            String extensionNum,
            String birthDate
    ) {
    }

    public record HrInfo(
            String orgName,
            String positionName,
            String rankName,
            String jobName,
            EmployeeState employeeState,
            String hireDate,
            EmployType employType,
            RecruitType recruitType,
            String areaName
    ) {
    }

    public record SkillItem(
            Long skillId,
            SkillCategory category,
            String skillName,
            String acquisitionDate,
            String licenseNumber,
            Long hrFileId
    ) {
    }

    public record CareerItem(
            Long careerId,
            String companyName,
            String orgName,
            String startDate,
            String endDate,
            Long hrFileId
    ) {
    }
}

