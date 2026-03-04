package com.reverse.hr;

import com.reverse.hr.dto.EmployeeProfileDTO;

public interface HrFacade {
    EmployeeProfileDTO getEmployeeProfile(Long employeeId);
}
