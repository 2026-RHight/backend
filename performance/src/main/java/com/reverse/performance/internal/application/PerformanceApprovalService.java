package com.reverse.performance.internal.application;

import com.reverse.core.exception.ForbiddenException;
import com.reverse.performance.internal.dto.request.PerformanceApprovalActionRequest;
import com.reverse.performance.internal.dto.response.PerformanceApprovalItemResponse;
import com.reverse.performance.internal.dto.response.PerformanceApprovalResponse;
import com.reverse.performance.internal.exception.PerformanceNotFoundException;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceApprovalService {

    private final PerformanceDashboardSummaryService performanceDashboardSummaryService;
    private final PerformanceHrMemberResolver performanceHrMemberResolver;
    private final PerformanceViewMapper performanceViewMapper;

    public PerformanceApprovalResponse getApprovalItems(Long employeeId) {
        // 조직 구조 기반으로 팀원 ID 목록 조회 (evaluation 테이블 독립)
        List<Long> orgMemberIds =
                performanceHrMemberResolver.getMyOrganizationMembers(employeeId).stream()
                        .filter(m -> !employeeId.equals(m.employeeId()))
                        .map(PerformanceHrMemberResolver.OrganizationMemberSnapshot::employeeId)
                        .toList();

        // evaluation 테이블 기반 접근 가능한 팀원 ID 목록도 합산
        List<Long> evalMemberIds = performanceViewMapper.findInquiryAccessibleTargetIds(employeeId);

        Set<Long> allMemberIds = new LinkedHashSet<>(orgMemberIds);
        allMemberIds.addAll(evalMemberIds);

        if (allMemberIds.isEmpty()) {
            return new PerformanceApprovalResponse(List.of(), List.of());
        }

        List<PerformanceViewMapper.ApprovalItemRow> rows =
                performanceViewMapper.findApprovalItemsByMemberIds(new ArrayList<>(allMemberIds));
        Map<Long, PerformanceHrMemberResolver.EmployeeProfileSnapshot> profiles =
                performanceHrMemberResolver.getEmployeeProfiles(
                        rows.stream()
                                .map(PerformanceViewMapper.ApprovalItemRow::targetEmployeeId)
                                .toList());
        List<PerformanceApprovalItemResponse> allItems =
                rows.stream()
                        .map(
                                row -> {
                                    PerformanceHrMemberResolver.EmployeeProfileSnapshot profile =
                                            profiles.get(row.targetEmployeeId());
                                    return new PerformanceApprovalItemResponse(
                                            row.id(),
                                            profile == null ? "-" : profile.employeeName(),
                                            profile == null
                                                    ? "-"
                                                    : defaultString(profile.orgName(), "-"),
                                            row.title(),
                                            row.date(),
                                            row.status(),
                                            row.type(),
                                            row.achievement(),
                                            row.progress(),
                                            row.phase());
                                })
                        .toList();
        List<PerformanceApprovalItemResponse> planItems = new ArrayList<>();
        List<PerformanceApprovalItemResponse> resultItems = new ArrayList<>();

        allItems.stream()
                .sorted(Comparator.comparing(PerformanceApprovalItemResponse::date).reversed())
                .forEach(
                        item -> {
                            if ("result".equalsIgnoreCase(item.phase())) {
                                resultItems.add(item);
                            } else {
                                planItems.add(item);
                            }
                        });

        return new PerformanceApprovalResponse(planItems, resultItems);
    }

    @Transactional
    public void approvePerformance(
            Long employeeId, Long performanceId, PerformanceApprovalActionRequest request) {
        Long targetEmployeeId = performanceViewMapper.findPerformanceOwnerId(performanceId);
        if (targetEmployeeId == null) {
            throw new PerformanceNotFoundException("승인할 성과를 찾을 수 없습니다.");
        }
        validateManagerAccess(employeeId, targetEmployeeId);
        int updated =
                performanceViewMapper.approvePerformance(
                        performanceId, blankToNull(request.comment()));
        if (updated == 0) {
            throw new PerformanceNotFoundException("승인할 성과를 찾을 수 없습니다.");
        }
        performanceDashboardSummaryService.recalculateCurrentMonth(targetEmployeeId);
    }

    @Transactional
    public void rejectPerformance(
            Long employeeId, Long performanceId, PerformanceApprovalActionRequest request) {
        Long targetEmployeeId = performanceViewMapper.findPerformanceOwnerId(performanceId);
        if (targetEmployeeId == null) {
            throw new PerformanceNotFoundException("반려할 성과를 찾을 수 없습니다.");
        }
        validateManagerAccess(employeeId, targetEmployeeId);
        int updated =
                performanceViewMapper.rejectPerformance(
                        performanceId, blankToNull(request.comment()));
        if (updated == 0) {
            throw new PerformanceNotFoundException("반려할 성과를 찾을 수 없습니다.");
        }
    }

    private void validateManagerAccess(Long managerId, Long memberEmployeeId) {
        Integer evalCount =
                performanceViewMapper.countInquiryAccessibleTarget(managerId, memberEmployeeId);
        if (evalCount != null && evalCount > 0) return;
        boolean hasOrgAccess =
                performanceHrMemberResolver.getMyOrganizationMembers(managerId).stream()
                        .anyMatch(m -> memberEmployeeId.equals(m.employeeId()));
        if (!hasOrgAccess) {
            throw new ForbiddenException("FORBIDDEN", "해당 성과를 처리할 권한이 없습니다.");
        }
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
