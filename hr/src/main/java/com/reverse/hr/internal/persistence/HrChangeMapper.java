package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.HrEventStatus;
import com.reverse.hr.internal.domain.enums.HrEventType;
import com.reverse.hr.internal.persistence.row.HrChangeCurrentInfoRow;
import com.reverse.hr.internal.persistence.row.HrChangeEmployeeSearchRow;
import com.reverse.hr.internal.persistence.row.HrChangeEventRow;
import com.reverse.hr.internal.persistence.row.HrChangePendingEventRow;
import com.reverse.hr.internal.persistence.row.HrChangeRoleOptionRow;
import com.reverse.hr.internal.persistence.row.HrChangeSimpleOptionRow;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HrChangeMapper {

    List<HrChangeEmployeeSearchRow> findEmployeesForHrChange(
            @Param("keyword") String keyword,
            @Param("orgId") Long orgId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countEmployeesForHrChange(@Param("keyword") String keyword, @Param("orgId") Long orgId);

    Optional<HrChangeCurrentInfoRow> findCurrentInfoByEmployeeId(
            @Param("employeeId") Long employeeId);

    List<Long> findRoleIdsByEmployeeId(@Param("employeeId") Long employeeId);

    List<String> findRoleCodesByEmployeeId(@Param("employeeId") Long employeeId);

    List<HrChangeSimpleOptionRow> findAllOrganizations();

    List<HrChangeSimpleOptionRow> findAllJobs();

    List<HrChangeSimpleOptionRow> findAllPositions();

    List<HrChangeSimpleOptionRow> findAllRanks();

    List<HrChangeSimpleOptionRow> findAllWorkingAreas();

    List<HrChangeRoleOptionRow> findAllRoles();

    Long findRoleIdByCode(@Param("roleCode") String roleCode);

    int existsOrganization(@Param("orgId") Long orgId);

    int existsJob(@Param("jobId") Long jobId);

    int existsPosition(@Param("positionId") Long positionId);

    int existsRank(@Param("rankId") Long rankId);

    int existsWorkingArea(@Param("areaId") Long areaId);

    int existsRole(@Param("roleId") Long roleId);

    int updateEmployeeState(
            @Param("employeeId") Long employeeId,
            @Param("employeeState") EmployeeState employeeState);

    int updateEmployeeHrInfo(
            @Param("employeeId") Long employeeId,
            @Param("orgId") Long orgId,
            @Param("positionId") Long positionId,
            @Param("rankId") Long rankId,
            @Param("jobId") Long jobId,
            @Param("employType") EmployType employType,
            @Param("areaId") Long areaId,
            @Param("effectiveFrom") LocalDate effectiveFrom);

    int deleteEmployeeRoles(@Param("employeeId") Long employeeId);

    int insertEmployeeRole(@Param("employeeId") Long employeeId, @Param("roleId") Long roleId);

    int insertHrEvent(
            @Param("employeeId") Long employeeId,
            @Param("eventType") HrEventType eventType,
            @Param("eventTitle") String eventTitle,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo,
            @Param("reason") String reason,
            @Param("beforeChange") String beforeChange,
            @Param("afterChange") String afterChange,
            @Param("targetOrgId") Long targetOrgId,
            @Param("targetJobId") Long targetJobId,
            @Param("targetPositionId") Long targetPositionId,
            @Param("targetRankId") Long targetRankId,
            @Param("targetEmployeeState") EmployeeState targetEmployeeState,
            @Param("targetEmployType") EmployType targetEmployType,
            @Param("targetAreaId") Long targetAreaId,
            @Param("targetEffectiveFrom") LocalDate targetEffectiveFrom,
            @Param("targetRoleIdsJson") String targetRoleIdsJson,
            @Param("sourceApprovalId") Long sourceApprovalId);

    Long findLastInsertedHrEventId();

    Long findEmployeeIdByApprovalId(@Param("approvalId") Long approvalId);

    int existsHrEventBySourceApprovalId(@Param("sourceApprovalId") Long sourceApprovalId);

    Integer acquireSchedulerLock(@Param("lockName") String lockName);

    Integer releaseSchedulerLock(@Param("lockName") String lockName);

    List<HrChangePendingEventRow> findDuePendingHrEvents(
            @Param("baseDate") LocalDate baseDate, @Param("limit") int limit);

    int markHrEventApplied(@Param("hrEventId") Long hrEventId);

    int closePreviousAppliedEventEffectiveTo(@Param("hrEventId") Long hrEventId);

    int markHrEventFailed(
            @Param("hrEventId") Long hrEventId, @Param("appliedError") String appliedError);

    List<HrChangeEventRow> findHrChangeEvents(
            @Param("eventType") HrEventType eventType,
            @Param("eventStatus") HrEventStatus eventStatus,
            @Param("employeeId") Long employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long countHrChangeEvents(
            @Param("eventType") HrEventType eventType,
            @Param("eventStatus") HrEventStatus eventStatus,
            @Param("employeeId") Long employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
