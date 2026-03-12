package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.HrEventStatus;
import com.reverse.hr.internal.domain.enums.HrEventType;
import com.reverse.hr.internal.domain.enums.RecruitType;
import com.reverse.hr.internal.domain.enums.SkillCategory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AdminEmployeeDetailResponseDTO(
        Long employeeId,
        String employeeNum,
        String employeeName,
        String profileFileUrl,
        String email,
        String phone,
        String extensionNum,
        String address,
        LocalDate birthDate,
        String orgName,
        String positionName,
        String jobName,
        String rankName,
        EmployeeState employeeState,
        String employeeStateDescription,
        LocalDate hireDate,
        EmployType employType,
        String employTypeDescription,
        RecruitType recruitType,
        String recruitTypeDescription,
        String areaName,
        String bankName,
        String residentNumberMasked,
        String accountNumberMasked,
        List<SkillItem> skills,
        List<CareerItem> careers,
        List<HrHistoryItem> hrHistories) {

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

    public record HrHistoryItem(
            Long hrEventId,
            HrEventType eventType,
            String eventTypeDescription,
            String eventTitle,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String reason,
            String beforeChange,
            String afterChange,
            Long sourceApprovalId,
            HrEventStatus eventStatus,
            String eventStatusDescription,
            LocalDateTime appliedAt,
            String appliedError) {}
}
