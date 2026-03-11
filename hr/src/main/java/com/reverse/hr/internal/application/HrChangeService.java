package com.reverse.hr.internal.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reverse.core.exception.NotFoundException;
import com.reverse.core.response.PageResponse;
import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.HrEventStatus;
import com.reverse.hr.internal.domain.enums.HrEventType;
import com.reverse.hr.internal.dto.request.HrChangeUpdateRequestDTO;
import com.reverse.hr.internal.dto.response.HrChangeCurrentInfoResponseDTO;
import com.reverse.hr.internal.dto.response.HrChangeEmployeeSearchItemResponseDTO;
import com.reverse.hr.internal.dto.response.HrChangeEventResponseDTO;
import com.reverse.hr.internal.dto.response.HrChangeOptionsResponseDTO;
import com.reverse.hr.internal.dto.response.HrChangeRoleOptionResponseDTO;
import com.reverse.hr.internal.dto.response.HrChangeSimpleOptionResponseDTO;
import com.reverse.hr.internal.dto.response.HrChangeUpdateResponseDTO;
import com.reverse.hr.internal.dto.response.OrganizationTreeNodeResponseDTO;
import com.reverse.hr.internal.persistence.HrChangeMapper;
import com.reverse.hr.internal.persistence.row.HrChangeCurrentInfoRow;
import com.reverse.hr.internal.persistence.row.HrChangeEmployeeSearchRow;
import com.reverse.hr.internal.persistence.row.HrChangeEventRow;
import com.reverse.hr.internal.persistence.row.HrChangePendingEventRow;
import com.reverse.hr.internal.persistence.row.HrChangeRoleOptionRow;
import com.reverse.hr.internal.persistence.row.HrChangeSimpleOptionRow;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HrChangeService {

    private static final String DEFAULT_EVALUATEE_ROLE_CODE = "EVALUATEE";
    private static final String SCHEDULER_LOCK_NAME = "hr_change_apply_scheduler_lock";

    @Value("${hr.change.apply.batch-size:200}")
    private int applyBatchSize;

    private final HrChangeMapper hrChangeMapper;
    private final OrganizationService organizationService;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    public List<OrganizationTreeNodeResponseDTO> getOrganizationTree() {
        return organizationService.getOrganizationTree();
    }

    public PageResponse<HrChangeEmployeeSearchItemResponseDTO> searchEmployees(
            String keyword, Long orgId, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(100, Math.max(1, size));
        int limit = safeSize;

        long offsetLong = (long) (safePage - 1) * safeSize;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("조회 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;

        long total = hrChangeMapper.countEmployeesForHrChange(keyword, orgId);
        List<HrChangeEmployeeSearchItemResponseDTO> content =
                hrChangeMapper.findEmployeesForHrChange(keyword, orgId, limit, offset).stream()
                        .map(this::toSearchItem)
                        .toList();

        return PageResponse.of(content, safePage, safeSize, total);
    }

    public HrChangeCurrentInfoResponseDTO getCurrentInfo(Long employeeId) {
        HrChangeCurrentInfoRow row =
                hrChangeMapper
                        .findCurrentInfoByEmployeeId(employeeId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "EMPLOYEE_NOT_FOUND", "사원 정보를 찾을 수 없습니다."));

        List<Long> roleIds = hrChangeMapper.findRoleIdsByEmployeeId(employeeId);
        List<String> roleCodes = hrChangeMapper.findRoleCodesByEmployeeId(employeeId);

        return new HrChangeCurrentInfoResponseDTO(
                row.employeeId(),
                row.employeeNum(),
                row.employeeName(),
                row.orgId(),
                row.orgName(),
                row.jobId(),
                row.jobName(),
                row.positionId(),
                row.positionName(),
                row.rankId(),
                row.rankName(),
                row.employeeState(),
                description(row.employeeState()),
                row.employType(),
                description(row.employType()),
                row.areaId(),
                row.areaName(),
                row.effectiveFrom(),
                roleIds,
                roleCodes);
    }

    public HrChangeOptionsResponseDTO getOptions() {
        return new HrChangeOptionsResponseDTO(
                mapSimpleOptions(hrChangeMapper.findAllOrganizations()),
                mapSimpleOptions(hrChangeMapper.findAllJobs()),
                mapSimpleOptions(hrChangeMapper.findAllPositions()),
                mapSimpleOptions(hrChangeMapper.findAllRanks()),
                mapSimpleOptions(hrChangeMapper.findAllWorkingAreas()),
                hrChangeMapper.findAllRoles().stream().map(this::toRoleOption).toList());
    }

    @Transactional
    public HrChangeUpdateResponseDTO updateEmployeeHrInfo(
            Long employeeId, HrChangeUpdateRequestDTO request) {
        HrChangeCurrentInfoRow before =
                hrChangeMapper
                        .findCurrentInfoByEmployeeId(employeeId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "EMPLOYEE_NOT_FOUND", "사원 정보를 찾을 수 없습니다."));

        Long resolvedOrgId = coalesce(request.orgId(), before.orgId());
        Long resolvedJobId = coalesce(request.jobId(), before.jobId());
        Long resolvedPositionId = coalesce(request.positionId(), before.positionId());
        Long resolvedRankId = coalesce(request.rankId(), before.rankId());
        EmployeeState resolvedEmployeeState =
                coalesce(request.employeeState(), before.employeeState());
        EmployType resolvedEmployType = coalesce(request.employType(), before.employType());
        Long resolvedAreaId = coalesce(request.areaId(), before.areaId());
        LocalDate resolvedEffectiveFrom = coalesce(request.effectiveFrom(), LocalDate.now());

        validateResolvedIds(
                resolvedOrgId, resolvedJobId, resolvedPositionId, resolvedRankId, resolvedAreaId);

        HrEventType eventType =
                resolveEventType(
                        before,
                        resolvedOrgId,
                        resolvedPositionId,
                        resolvedRankId,
                        resolvedEmployeeState);

        String beforeChange = buildBeforeChange(before);
        String afterChange =
                buildAfterChange(
                        resolvedOrgId,
                        resolvedJobId,
                        resolvedPositionId,
                        resolvedRankId,
                        resolvedEmployeeState,
                        resolvedEmployType,
                        resolvedAreaId);

        String targetRoleIdsJson = toTargetRoleIdsJson(request.roleIds());

        int inserted =
                hrChangeMapper.insertHrEvent(
                        employeeId,
                        eventType,
                        buildEventTitle(eventType),
                        resolvedEffectiveFrom,
                        null,
                        request.reason(),
                        beforeChange,
                        afterChange,
                        resolvedOrgId,
                        resolvedJobId,
                        resolvedPositionId,
                        resolvedRankId,
                        resolvedEmployeeState,
                        resolvedEmployType,
                        resolvedAreaId,
                        resolvedEffectiveFrom,
                        targetRoleIdsJson);
        if (inserted != 1) {
            throw new IllegalStateException("인사 변경 요청 저장 중 오류가 발생했습니다.");
        }

        Long hrEventId = hrChangeMapper.findLastInsertedHrEventId();
        return new HrChangeUpdateResponseDTO(
                employeeId, hrEventId, eventType, description(eventType));
    }

    @Scheduled(cron = "${hr.change.apply.cron:0 5 0 * * *}", zone = "Asia/Seoul")
    public void applyDueHrEventsDaily() {
        int processed = applyDueHrEvents(LocalDate.now());
        if (processed > 0) {
            log.info("Applied {} pending hr events", processed);
        }
    }

    public int applyDueHrEvents(LocalDate baseDate) {
        Integer lockResult = hrChangeMapper.acquireSchedulerLock(SCHEDULER_LOCK_NAME);
        if (lockResult == null || lockResult != 1) {
            log.debug("Skip hr event scheduler because lock is held by another worker.");
            return 0;
        }

        try {
            return applyDueHrEventsWithTransaction(baseDate);
        } finally {
            hrChangeMapper.releaseSchedulerLock(SCHEDULER_LOCK_NAME);
        }
    }

    public PageResponse<HrChangeEventResponseDTO> getHrChangeEvents(
            HrEventType eventType,
            Long employeeId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(100, Math.max(1, size));
        int limit = safeSize;

        long offsetLong = (long) (safePage - 1) * safeSize;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("조회 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;

        long total = hrChangeMapper.countHrChangeEvents(eventType, employeeId, fromDate, toDate);
        List<HrChangeEventResponseDTO> content =
                hrChangeMapper
                        .findHrChangeEvents(eventType, employeeId, fromDate, toDate, limit, offset)
                        .stream()
                        .map(this::toEventResponse)
                        .toList();

        return PageResponse.of(content, safePage, safeSize, total);
    }

    private int applyDueHrEventsWithTransaction(LocalDate baseDate) {
        int totalProcessed = 0;
        int safeBatchSize = Math.max(1, applyBatchSize);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        while (true) {
            List<HrChangePendingEventRow> dueEvents =
                    hrChangeMapper.findDuePendingHrEvents(baseDate, safeBatchSize);
            if (dueEvents.isEmpty()) {
                break;
            }

            for (HrChangePendingEventRow pendingEvent : dueEvents) {
                try {
                    txTemplate.executeWithoutResult(status -> applySingleEvent(pendingEvent));
                    totalProcessed++;
                } catch (Exception ex) {
                    log.warn(
                            "Failed to apply hr_event_id={}: {}",
                            pendingEvent.hrEventId(),
                            ex.getMessage());
                    txTemplate.executeWithoutResult(
                            status ->
                                    hrChangeMapper.markHrEventFailed(
                                            pendingEvent.hrEventId(),
                                            abbreviate(ex.getMessage(), 500)));
                }
            }

            if (dueEvents.size() < safeBatchSize) {
                break;
            }
        }

        return totalProcessed;
    }

    private void applySingleEvent(HrChangePendingEventRow pendingEvent) {
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

    private String toTargetRoleIdsJson(List<Long> requestedRoleIds) {
        if (requestedRoleIds == null) {
            return null;
        }

        Long evaluateeRoleId = hrChangeMapper.findRoleIdByCode(DEFAULT_EVALUATEE_ROLE_CODE);
        if (evaluateeRoleId == null) {
            throw new IllegalStateException("기본 역할(EVALUATEE)을 찾을 수 없습니다.");
        }

        Set<Long> merged = new LinkedHashSet<>();
        merged.add(evaluateeRoleId);
        requestedRoleIds.stream().filter(Objects::nonNull).forEach(merged::add);

        for (Long roleId : merged) {
            if (hrChangeMapper.existsRole(roleId) == 0) {
                throw new IllegalArgumentException("유효하지 않은 권한 ID입니다: " + roleId);
            }
        }

        try {
            return objectMapper.writeValueAsString(new ArrayList<>(merged));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("권한 정보 직렬화 중 오류가 발생했습니다.", e);
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

    private HrEventType resolveEventType(
            HrChangeCurrentInfoRow before,
            Long resolvedOrgId,
            Long resolvedPositionId,
            Long resolvedRankId,
            EmployeeState resolvedEmployeeState) {
        if (!Objects.equals(before.rankId(), resolvedRankId)) {
            return HrEventType.PROMOTION;
        }
        if (!Objects.equals(before.orgId(), resolvedOrgId)) {
            return HrEventType.TRANSFER;
        }
        if (!Objects.equals(before.positionId(), resolvedPositionId)) {
            return HrEventType.POSITION_CHANGE;
        }
        if (!Objects.equals(before.employeeState(), resolvedEmployeeState)) {
            return HrEventType.STATE_CHANGE;
        }
        return HrEventType.ORG_CHANGE;
    }

    private String buildBeforeChange(HrChangeCurrentInfoRow before) {
        return "조직:"
                + valueOrDash(before.orgName())
                + ", 직무:"
                + valueOrDash(before.jobName())
                + ", 직책:"
                + valueOrDash(before.positionName())
                + ", 직급:"
                + valueOrDash(before.rankName())
                + ", 재직상태:"
                + description(before.employeeState())
                + ", 근무형태:"
                + description(before.employType())
                + ", 근무지:"
                + valueOrDash(before.areaName());
    }

    private String buildAfterChange(
            Long resolvedOrgId,
            Long resolvedJobId,
            Long resolvedPositionId,
            Long resolvedRankId,
            EmployeeState resolvedEmployeeState,
            EmployType resolvedEmployType,
            Long resolvedAreaId) {
        Map<Long, String> orgMap = optionMap(hrChangeMapper.findAllOrganizations());
        Map<Long, String> jobMap = optionMap(hrChangeMapper.findAllJobs());
        Map<Long, String> positionMap = optionMap(hrChangeMapper.findAllPositions());
        Map<Long, String> rankMap = optionMap(hrChangeMapper.findAllRanks());
        Map<Long, String> areaMap = optionMap(hrChangeMapper.findAllWorkingAreas());

        return "조직:"
                + valueOrDash(orgMap.get(resolvedOrgId))
                + ", 직무:"
                + valueOrDash(jobMap.get(resolvedJobId))
                + ", 직책:"
                + valueOrDash(positionMap.get(resolvedPositionId))
                + ", 직급:"
                + valueOrDash(rankMap.get(resolvedRankId))
                + ", 재직상태:"
                + description(resolvedEmployeeState)
                + ", 근무형태:"
                + description(resolvedEmployType)
                + ", 근무지:"
                + valueOrDash(areaMap.get(resolvedAreaId));
    }

    private String buildEventTitle(HrEventType eventType) {
        return description(eventType) + " 처리";
    }

    private HrChangeEmployeeSearchItemResponseDTO toSearchItem(HrChangeEmployeeSearchRow row) {
        return new HrChangeEmployeeSearchItemResponseDTO(
                row.employeeId(),
                row.employeeNum(),
                row.employeeName(),
                row.orgName(),
                row.positionName(),
                row.jobName(),
                row.rankName(),
                row.employeeState(),
                description(row.employeeState()),
                row.employType(),
                description(row.employType()),
                row.areaName());
    }

    private HrChangeRoleOptionResponseDTO toRoleOption(HrChangeRoleOptionRow row) {
        return new HrChangeRoleOptionResponseDTO(
                row.roleId(), row.roleCode(), row.roleName(), row.description());
    }

    private List<HrChangeSimpleOptionResponseDTO> mapSimpleOptions(
            List<HrChangeSimpleOptionRow> rows) {
        return rows.stream()
                .map(row -> new HrChangeSimpleOptionResponseDTO(row.id(), row.name()))
                .toList();
    }

    private Map<Long, String> optionMap(List<HrChangeSimpleOptionRow> rows) {
        return rows.stream()
                .collect(
                        Collectors.toMap(
                                HrChangeSimpleOptionRow::id, HrChangeSimpleOptionRow::name));
    }

    private HrChangeEventResponseDTO toEventResponse(HrChangeEventRow row) {
        return new HrChangeEventResponseDTO(
                row.hrEventId(),
                row.employeeId(),
                row.employeeName(),
                row.eventType(),
                description(row.eventType()),
                row.eventTitle(),
                row.beforeChange(),
                row.afterChange(),
                row.reason(),
                row.effectiveFrom(),
                row.effectiveTo(),
                null,
                row.eventStatus(),
                description(row.eventStatus()),
                row.appliedAt(),
                row.appliedError());
    }

    private String description(EmployeeState value) {
        return value == null ? "-" : value.getDescription();
    }

    private String description(EmployType value) {
        return value == null ? "-" : value.getDescription();
    }

    private String description(HrEventType value) {
        return value == null ? "-" : value.getDescription();
    }

    private String description(HrEventStatus value) {
        return value == null ? "-" : value.getDescription();
    }

    private <T> T coalesce(T requested, T current) {
        return requested != null ? requested : current;
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String abbreviate(String value, int maxLen) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN_ERROR";
        }
        return value.length() > maxLen ? value.substring(0, maxLen) : value;
    }
}
