package com.reverse.hr.internal.application;

import com.reverse.core.exception.UnauthorizedException;
import com.reverse.hr.internal.dto.response.OrganizationMemberResponseDTO;
import com.reverse.hr.internal.dto.response.OrganizationTreeNodeResponseDTO;
import com.reverse.hr.internal.persistence.OrganizationMapper;
import com.reverse.hr.internal.persistence.row.OrgMemberRow;
import com.reverse.hr.internal.persistence.row.OrgTreeNodeRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationMapper organizationMapper;


    public List<OrganizationTreeNodeResponseDTO> getOrganizationTree() {
        List<OrgTreeNodeRow> rows = organizationMapper.findOrganizationTree();
        return rows.stream()
                .map(row -> new OrganizationTreeNodeResponseDTO(
                        row.orgId(),
                        row.parentOrgId(),
                        row.orgName(),
                        row.orgType(),
                        row.orgLevel(),
                        row.sortOrder(),
                        row.memberCount()
                ))
                .toList();
    }

    public List<OrganizationMemberResponseDTO> getOrganizationMembers(Long viewerEmployeeId, Long orgId) {
        validateSameTeamAccess(viewerEmployeeId, orgId);
        List<OrgMemberRow> rows = organizationMapper.findOrganizationMembers(orgId);
        return rows.stream()
                .map(row -> new OrganizationMemberResponseDTO(
                        row.employeeId(),
                        row.employeeName(),
                        row.email(),
                        row.phone(),
                        row.extensionNum(),
                        row.positionName(),
                        row.jobName(),
                        row.rankName(),
                        row.areaName(),
                        row.employeeState()
                ))
                .toList();
    }

    public List<OrganizationMemberResponseDTO> getMyOrganizationMembers(Long employeeId) {
        Long orgId = organizationMapper.findMyOrgIdByEmployeeId(employeeId);
        if (orgId == null) {
            throw new IllegalStateException("소속 조직 정보가 없습니다.");
        }
        return getOrganizationMembers(employeeId, orgId);
    }

    private void validateSameTeamAccess(Long viewerEmployeeId, Long targetOrgId) {
        Long myOrgId = organizationMapper.findMyOrgIdByEmployeeId(viewerEmployeeId);
        if (myOrgId == null) {
            throw new IllegalStateException("소속 조직 정보가 없습니다.");
        }

        if (!myOrgId.equals(targetOrgId)) {
            throw new UnauthorizedException("ORG_MEMBER_ACCESS_DENIED", "같은 팀 구성원만 조회할 수 있습니다.");
        }
    }
}
