package com.reverse.performance.internal.application;

import com.reverse.hr.HrFacade;
import com.reverse.hr.dto.EmployeeProfileDTO;
import com.reverse.hr.dto.OrganizationMemberInfo;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerformanceHrMemberResolver {

    private final HrFacade hrFacade;

    public List<OrganizationMemberSnapshot> getMyOrganizationMembers(Long employeeId) {
        return hrFacade.getMyOrganizationMembers(employeeId).stream()
                .map(this::toOrganizationMemberSnapshot)
                .toList();
    }

    public EmployeeProfileSnapshot getEmployeeProfile(Long employeeId) {
        EmployeeProfileDTO profile = hrFacade.getEmployeeProfile(employeeId);
        return new EmployeeProfileSnapshot(
                profile.employeeId(),
                profile.employeeName(),
                profile.email(),
                profile.orgName(),
                profile.rankName(),
                profile.positionName(),
                profile.jobName());
    }

    public Map<Long, EmployeeProfileSnapshot> getEmployeeProfiles(List<Long> employeeIds) {
        return employeeIds.stream()
                .distinct()
                .collect(Collectors.toMap(Function.identity(), this::getEmployeeProfile));
    }

    public Long resolveOrgId(Long employeeId) {
        List<OrganizationMemberSnapshot> members = getMyOrganizationMembers(employeeId);
        return members.stream()
                .filter(member -> employeeId.equals(member.employeeId()))
                .map(OrganizationMemberSnapshot::orgId)
                .findFirst()
                .orElseGet(
                        () ->
                                members.stream()
                                        .map(OrganizationMemberSnapshot::orgId)
                                        .filter(id -> id != null)
                                        .findFirst()
                                        .orElse(null));
    }

    private OrganizationMemberSnapshot toOrganizationMemberSnapshot(OrganizationMemberInfo info) {
        return new OrganizationMemberSnapshot(
                info.employeeId(),
                info.employeeName(),
                info.orgId(),
                info.orgName(),
                info.positionId(),
                info.positionName(),
                info.rankName(),
                info.jobName());
    }

    public record OrganizationMemberSnapshot(
            Long employeeId,
            String employeeName,
            Long orgId,
            String orgName,
            Long positionId,
            String positionName,
            String rankName,
            String jobName) {}

    public record EmployeeProfileSnapshot(
            Long employeeId,
            String employeeName,
            String email,
            String orgName,
            String rankName,
            String positionName,
            String jobName) {}
}
