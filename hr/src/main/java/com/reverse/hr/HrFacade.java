package com.reverse.hr;

import com.reverse.hr.internal.EmployeeProfileFacadeResponse;

public interface HrFacade {
    EmployeeProfileFacadeResponse getEmployeeProfile(Long employeeId);
}
