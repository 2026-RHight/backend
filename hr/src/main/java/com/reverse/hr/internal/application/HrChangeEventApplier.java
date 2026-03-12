package com.reverse.hr.internal.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reverse.hr.internal.persistence.HrChangeMapper;
import com.reverse.hr.internal.persistence.row.HrChangePendingEventRow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HrChangeEventApplier {

    private final HrChangeMapper hrChangeMapper;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyInNewTransaction(HrChangePendingEventRow pendingEvent) {
        validateResolvedIds(
                pendingEvent.targetOrgId(),
                pendingEvent.targetJobId(),
                pendingEvent.targetPositionId(),
                pendingEvent.targetRankId(),
                pendingEvent.targetAreaId());

        int updatedEmp =
                hrChangeMapper.updateEmployeeState(
                        pendingEvent.employeeId(), pendingEvent.targetEmployeeState());
        int updatedHrInfo =
                hrChangeMapper.updateEmployeeHrInfo(
                        pendingEvent.employeeId(),
                        pendingEvent.targetOrgId(),
                        pendingEvent.targetPositionId(),
                        pendingEvent.targetRankId(),
                        pendingEvent.targetJobId(),
                        pendingEvent.targetEmployType(),
                        pendingEvent.targetAreaId(),
                        pendingEvent.targetEffectiveFrom());

        if (updatedEmp != 1 || updatedHrInfo != 1) {
            throw new IllegalStateException("인사 정보 반영 중 오류가 발생했습니다.");
        }

        if (pendingEvent.targetRoleIdsJson() != null
                && !pendingEvent.targetRoleIdsJson().isBlank()) {
            replaceRolesWithExactSet(
                    pendingEvent.employeeId(),
                    parseRoleIdsFromJson(pendingEvent.targetRoleIdsJson()));
        }

        int marked = hrChangeMapper.markHrEventApplied(pendingEvent.hrEventId());
        if (marked != 1) {
            throw new IllegalStateException("인사 이벤트 상태 반영 중 오류가 발생했습니다.");
        }
    }

    private void validateResolvedIds(
            Long orgId, Long jobId, Long positionId, Long rankId, Long areaId) {
        if (orgId == null
                || jobId == null
                || positionId == null
                || rankId == null
                || areaId == null) {
            throw new IllegalArgumentException("현재 인사 정보가 누락되어 변경할 수 없습니다.");
        }
        if (hrChangeMapper.existsOrganization(orgId) == 0) {
            throw new IllegalArgumentException("유효하지 않은 조직 ID입니다.");
        }
        if (hrChangeMapper.existsJob(jobId) == 0) {
            throw new IllegalArgumentException("유효하지 않은 직무 ID입니다.");
        }
        if (hrChangeMapper.existsPosition(positionId) == 0) {
            throw new IllegalArgumentException("유효하지 않은 직책 ID입니다.");
        }
        if (hrChangeMapper.existsRank(rankId) == 0) {
            throw new IllegalArgumentException("유효하지 않은 직급 ID입니다.");
        }
        if (hrChangeMapper.existsWorkingArea(areaId) == 0) {
            throw new IllegalArgumentException("유효하지 않은 근무지 ID입니다.");
        }
    }

    private void replaceRolesWithExactSet(Long employeeId, List<Long> roleIds) {
        for (Long roleId : roleIds) {
            if (hrChangeMapper.existsRole(roleId) == 0) {
                throw new IllegalArgumentException("유효하지 않은 권한 ID입니다: " + roleId);
            }
        }

        hrChangeMapper.deleteEmployeeRoles(employeeId);
        for (Long roleId : roleIds) {
            hrChangeMapper.insertEmployeeRole(employeeId, roleId);
        }
    }

    private List<Long> parseRoleIdsFromJson(String targetRoleIdsJson) {
        try {
            List<Long> parsed = objectMapper.readValue(targetRoleIdsJson, new TypeReference<>() {});
            return parsed == null ? List.of() : parsed;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("권한 정보 역직렬화 중 오류가 발생했습니다.", e);
        }
    }
}
