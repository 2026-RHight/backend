package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.persistence.row.EmployeeProfileFacadeRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface EmployeeFacadeMapper {
    Optional<EmployeeProfileFacadeRow> findEmployeeProfileById(@Param("employeeId") Long employeeId);
}
