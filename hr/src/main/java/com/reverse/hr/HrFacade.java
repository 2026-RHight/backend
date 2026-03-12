package com.reverse.hr;

import com.reverse.hr.dto.EmployeeProfileDTO;
import com.reverse.hr.dto.OrganizationMemberInfo;
import java.util.List;

public interface HrFacade {
    EmployeeProfileDTO getEmployeeProfile(Long employeeId);

    List<OrganizationMemberInfo> getMyOrganizationMembers(Long employeeId);
}
