package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.RecruitType;
import com.reverse.hr.internal.domain.enums.SensitiveFieldType;
import com.reverse.hr.internal.persistence.row.AdminEmployeeDetailRow;
import com.reverse.hr.internal.persistence.row.AdminEmployeeListRow;
import com.reverse.hr.internal.persistence.row.HrEventItemRow;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminEmployeeMapper {

    int existsOrganization(@Param("orgId") Long orgId);

    int existsJob(@Param("jobId") Long jobId);

    int existsPosition(@Param("positionId") Long positionId);

    int existsRank(@Param("rankId") Long rankId);

    int existsWorkingArea(@Param("areaId") Long areaId);

    int existsRole(@Param("roleId") Long roleId);

    Long findRoleIdByCode(@Param("roleCode") String roleCode);

    Integer findMaxDailyEmployeeSequence(@Param("datePrefix") String datePrefix);

    int insertDefaultProfileFile(
            @Param("fileUrl") String fileUrl, @Param("fileTitle") String fileTitle);

    Long findLastInsertedHrFileId();

    int insertEmployee(
            @Param("employeeNum") String employeeNum,
            @Param("employeeName") String employeeName,
            @Param("employeePassword") String employeePassword,
            @Param("phone") String phone,
            @Param("extensionNum") String extensionNum,
            @Param("email") String email,
            @Param("address") String address,
            @Param("birthDate") LocalDate birthDate,
            @Param("bankName") String bankName,
            @Param("accountNumberEnc") String accountNumberEnc,
            @Param("accountNumberHash") String accountNumberHash,
            @Param("residentNumberEnc") String residentNumberEnc,
            @Param("residentNumberHash") String residentNumberHash,
            @Param("initialState") boolean initialState,
            @Param("employeeState") EmployeeState employeeState,
            @Param("hireDate") LocalDate hireDate,
            @Param("profileId") Long profileId);

    Long findLastInsertedEmployeeId();

    int insertEmployeeHrInfo(
            @Param("employeeId") Long employeeId,
            @Param("orgId") Long orgId,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("positionId") Long positionId,
            @Param("rankId") Long rankId,
            @Param("jobId") Long jobId,
            @Param("employType") EmployType employType,
            @Param("recruitType") RecruitType recruitType,
            @Param("areaId") Long areaId);

    int insertEmployeeRole(@Param("employeeId") Long employeeId, @Param("roleId") Long roleId);

    int insertPasswordHistory(
            @Param("employeeId") Long employeeId, @Param("passwordHash") String passwordHash);

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
