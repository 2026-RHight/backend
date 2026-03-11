package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.SensitiveFieldType;
import com.reverse.hr.internal.persistence.row.AdminEmployeeDetailRow;
import com.reverse.hr.internal.persistence.row.AdminEmployeeListRow;
import com.reverse.hr.internal.persistence.row.HrEventItemRow;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminEmployeeMapper {

    List<AdminEmployeeListRow> findEmployees(
            @Param("keyword") String keyword,
            @Param("orgId") Long orgId,
            @Param("employeeState") EmployeeState employeeState,
            @Param("employType") EmployType employType,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countEmployees(
            @Param("keyword") String keyword,
            @Param("orgId") Long orgId,
            @Param("employeeState") EmployeeState employeeState,
            @Param("employType") EmployType employType);

    Optional<AdminEmployeeDetailRow> findEmployeeDetailById(@Param("employeeId") Long employeeId);

    List<HrEventItemRow> findHrEventsByEmployeeId(@Param("employeeId") Long employeeId);

    int insertSensitiveAccessLog(
            @Param("viewerEmployeeId") Long viewerEmployeeId,
            @Param("targetEmployeeId") Long targetEmployeeId,
            @Param("fieldType") SensitiveFieldType fieldType,
            @Param("reason") String reason);
}
