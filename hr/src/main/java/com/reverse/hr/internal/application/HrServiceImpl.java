package com.reverse.hr.internal.application;

import com.reverse.core.security.EmployeeAuthInfoDTO;
import com.reverse.core.security.EmployeeAuthProvider;
import com.reverse.hr.HrFacade;
import com.reverse.hr.dto.EmployeeProfileDTO;
import com.reverse.hr.dto.OrganizationMemberInfo;
import com.reverse.hr.internal.persistence.EmployeeFacadeMapper;
import com.reverse.hr.internal.persistence.EmployeeMapper;
import com.reverse.hr.internal.persistence.row.EmployeeProfileFacadeRow;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HrServiceImpl implements EmployeeAuthProvider, HrFacade {

    private final EmployeeMapper employeeMapper;
    private final EmployeeFacadeMapper employeeFacadeMapper;

    // === EmployeeAuthProvider 구현 (core용) ===

    @Override
    public Optional<EmployeeAuthInfoDTO> findByEmployeeNum(String employeeNum) {
        return employeeMapper.findUserByEmployeeNum(employeeNum);
    }

    @Override
    public List<String> findRolesByEmployeeId(Long employeeId) {
        return employeeMapper.findRolesByEmployeeId(employeeId);
    }

    @Override
    public EmployeeProfileDTO getEmployeeProfile(Long employeeId) {
        EmployeeProfileFacadeRow row =
                employeeFacadeMapper
                        .findEmployeeProfileById(employeeId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "사원을 찾을 수 없습니다. employeeId=" + employeeId));

        return new EmployeeProfileDTO(
                row.employeeId(),
                row.employeeName(),
                row.email(),
                row.orgName(),
                row.rankName(),
                row.positionName(),
                row.jobName());
    }

    @Override
    public List<OrganizationMemberInfo> getMyOrganizationMembers(Long employeeId) {
        return employeeFacadeMapper.findMyOrganizationMembers(employeeId).stream()
                .map(
                        row ->
                                new OrganizationMemberInfo(
                                        row.employeeId(),
                                        row.employeeName(),
                                        row.orgId(),
                                        row.orgName(),
                                        row.positionId(),
                                        row.positionName(),
                                        row.rankName(),
                                        row.jobName()))
                .toList();
    }

    // === HrFacade 구현 (다른 도메인 모듈용) ===
    // 나중에 다른 모듈이 필요한 메서드 추가
}
