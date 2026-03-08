package com.reverse.hr.internal.application;

import com.reverse.core.exception.NotFoundException;
import com.reverse.core.exception.UnauthorizedException;
import com.reverse.hr.internal.dto.response.EvidenceFileResponseDTO;
import com.reverse.hr.internal.dto.response.OrganizationMemberDetailResponseDTO;
import com.reverse.hr.internal.dto.response.OrganizationMemberResponseDTO;
import com.reverse.hr.internal.dto.response.OrganizationTreeNodeResponseDTO;
import com.reverse.hr.internal.persistence.MyPageMapper;
import com.reverse.hr.internal.persistence.OrganizationMapper;
import com.reverse.hr.internal.persistence.row.BasicInfoRow;
import com.reverse.hr.internal.persistence.row.CareerItemRow;
import com.reverse.hr.internal.persistence.row.HrFileRow;
import com.reverse.hr.internal.persistence.row.HrInfoRow;
import com.reverse.hr.internal.persistence.row.OrgMemberRow;
import com.reverse.hr.internal.persistence.row.OrgTreeNodeRow;
import com.reverse.hr.internal.persistence.row.SkillItemRow;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationMapper organizationMapper;
    private final MyPageMapper myPageMapper;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public List<OrganizationTreeNodeResponseDTO> getOrganizationTree() {
        List<OrgTreeNodeRow> rows = organizationMapper.findOrganizationTree();
        return rows.stream()
                .map(
                        row ->
                                new OrganizationTreeNodeResponseDTO(
                                        row.orgId(),
                                        row.parentOrgId(),
                                        row.orgName(),
                                        row.orgType(),
                                        row.orgLevel(),
                                        row.sortOrder(),
                                        row.memberCount()))
                .toList();
    }

    public List<OrganizationMemberResponseDTO> getOrganizationMembers(Long orgId) {
        List<OrgMemberRow> rows = organizationMapper.findOrganizationMembers(orgId);
        return rows.stream()
                .map(
                        row ->
                                new OrganizationMemberResponseDTO(
                                        row.employeeId(),
                                        row.employeeName(),
                                        row.email(),
                                        row.phone(),
                                        row.extensionNum(),
                                        row.positionName(),
                                        row.jobName(),
                                        row.rankName(),
                                        row.areaName(),
                                        row.employeeState()))
                .toList();
    }

    public List<OrganizationMemberResponseDTO> getMyOrganizationMembers(Long employeeId) {
        Long orgId = organizationMapper.findMyOrgIdByEmployeeId(employeeId);
        if (orgId == null) {
            throw new NotFoundException("ORG_NOT_FOUND", "소속 조직 정보가 없습니다.");
        }
        return getOrganizationMembers(orgId);
    }

    public OrganizationMemberDetailResponseDTO getOrganizationMemberDetail(
            Long viewerEmployeeId, Long targetEmployeeId) {
        validateSameTeamAccess(viewerEmployeeId, targetEmployeeId);

        BasicInfoRow basicInfoRow =
                myPageMapper
                        .findBasicInfoByEmployeeId(targetEmployeeId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "EMPLOYEE_BASIC_INFO_NOT_FOUND",
                                                "기본 정보를 찾을 수 없습니다."));

        HrInfoRow hrInfoRow =
                myPageMapper
                        .findHrInfoByEmployeeId(targetEmployeeId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "EMPLOYEE_HR_INFO_NOT_FOUND", "인사 정보를 찾을 수 없습니다."));

        List<SkillItemRow> skillRows = myPageMapper.findSkillsByEmployeeId(targetEmployeeId);
        List<CareerItemRow> careerRows = myPageMapper.findCareersByEmployeeId(targetEmployeeId);

        OrganizationMemberDetailResponseDTO.PersonalInfo personalInfo =
                new OrganizationMemberDetailResponseDTO.PersonalInfo(
                        basicInfoRow.employeeName(),
                        basicInfoRow.email(),
                        basicInfoRow.phone(),
                        basicInfoRow.extensionNum(),
                        formatDate(basicInfoRow.birthDate()));

        OrganizationMemberDetailResponseDTO.HrInfo hrInfo =
                new OrganizationMemberDetailResponseDTO.HrInfo(
                        hrInfoRow.orgName(),
                        hrInfoRow.positionName(),
                        hrInfoRow.rankName(),
                        hrInfoRow.jobName(),
                        hrInfoRow.employeeState(),
                        formatDate(hrInfoRow.hireDate()),
                        hrInfoRow.employType(),
                        hrInfoRow.recruitType(),
                        hrInfoRow.areaName());

        List<OrganizationMemberDetailResponseDTO.SkillItem> skills =
                skillRows.stream()
                        .map(
                                row ->
                                        new OrganizationMemberDetailResponseDTO.SkillItem(
                                                row.skillId(),
                                                row.category(),
                                                row.skillName(),
                                                row.acquisitionDate(),
                                                row.licenseNumber(),
                                                row.hrFileId()))
                        .toList();

        List<OrganizationMemberDetailResponseDTO.CareerItem> careers =
                careerRows.stream()
                        .map(
                                row ->
                                        new OrganizationMemberDetailResponseDTO.CareerItem(
                                                row.careerId(),
                                                row.companyName(),
                                                row.orgName(),
                                                row.startDate(),
                                                row.endDate(),
                                                row.hrFileId()))
                        .toList();

        return new OrganizationMemberDetailResponseDTO(personalInfo, hrInfo, skills, careers);
    }

    public EvidenceFileResponseDTO getOrganizationMemberSkillEvidence(
            Long viewerEmployeeId, Long targetEmployeeId, Long skillId) {
        validateSameTeamAccess(viewerEmployeeId, targetEmployeeId);
        HrFileRow fileRow =
                myPageMapper
                        .findSkillFileByIdAndEmployeeId(targetEmployeeId, skillId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "EVIDENCE_NOT_FOUND", "증빙 파일을 찾을 수 없습니다."));

        return new EvidenceFileResponseDTO(
                fileRow.getHrFileId(), fileRow.getFileTitle(), fileRow.getFileUrl());
    }

    public EvidenceFileResponseDTO getOrganizationMemberCareerEvidence(
            Long viewerEmployeeId, Long targetEmployeeId, Long careerId) {
        validateSameTeamAccess(viewerEmployeeId, targetEmployeeId);
        HrFileRow fileRow =
                myPageMapper
                        .findCareerFileByIdAndEmployeeId(targetEmployeeId, careerId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "EVIDENCE_NOT_FOUND", "증빙 파일을 찾을 수 없습니다."));

        return new EvidenceFileResponseDTO(
                fileRow.getHrFileId(), fileRow.getFileTitle(), fileRow.getFileUrl());
    }

    private void validateSameTeamAccess(Long viewerEmployeeId, Long targetEmployeeId) {
        Long myOrgId = organizationMapper.findMyOrgIdByEmployeeId(viewerEmployeeId);
        Long targetOrgId = organizationMapper.findMyOrgIdByEmployeeId(targetEmployeeId);

        if (myOrgId == null) {
            throw new NotFoundException("ORG_NOT_FOUND", "소속 조직 정보가 없습니다.");
        }

        if (targetOrgId == null) {
            throw new NotFoundException("TARGET_ORG_NOT_FOUND", "대상 사원의 소속 조직 정보가 없습니다.");
        }

        if (!myOrgId.equals(targetOrgId)) {
            throw new UnauthorizedException("ORG_MEMBER_ACCESS_DENIED", "같은 팀 구성원만 조회할 수 있습니다.");
        }
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }
}
