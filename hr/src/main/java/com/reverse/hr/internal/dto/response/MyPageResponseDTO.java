package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.RecruitType;
import com.reverse.hr.internal.domain.enums.SkillCategory;
import java.util.List;

public record MyPageResponseDTO(
        BasicInfo basicInfo, HrInfo hrInfo, List<SkillItem> skills, List<CareerItem> careers) {
    public record BasicInfo(
            String employeeNum,
            String employeeName,
            String email,
            String phone,
            String extensionNum,
            String birthDate,
            String address,
            String residentNumberMasked,
            String bankName,
            String accountNumberMasked,
            String profileFileUrl) {}

    public record HrInfo(
            String orgName,
            String positionName,
            String rankName,
            String jobName,
            EmployeeState employeeState,
            String employeeStateDescription,
            String hireDate,
            String tenureText,
            EmployType employType,
            String employTypeDescription,
            RecruitType recruitType,
            String recruitTypeDescription,
            String areaName) {}

    public record SkillItem(
            Long skillId,
            SkillCategory category,
            String skillName,
            String acquisitionDate,
            String licenseNumber,
            Long hrFileId) {}

    public record CareerItem(
            Long careerId,
            String companyName,
            String orgName,
            String startDate,
            String endDate,
            Long hrFileId) {}
}
