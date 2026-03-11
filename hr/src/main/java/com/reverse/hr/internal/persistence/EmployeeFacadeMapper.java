package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.persistence.row.EmployeeProfileFacadeRow;
import com.reverse.hr.internal.persistence.row.OrganizationMemberFacadeRow;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmployeeFacadeMapper {
    Optional<EmployeeProfileFacadeRow> findEmployeeProfileById(
            @Param("employeeId") Long employeeId);

    List<OrganizationMemberFacadeRow> findMyOrganizationMembers(
            @Param("employeeId") Long employeeId);
}
