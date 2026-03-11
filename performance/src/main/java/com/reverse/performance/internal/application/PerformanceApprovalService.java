package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.request.PerformanceApprovalActionRequest;
import com.reverse.performance.internal.dto.response.PerformanceApprovalItemResponse;
import com.reverse.performance.internal.dto.response.PerformanceApprovalResponse;
import com.reverse.performance.internal.exception.PerformanceNotFoundException;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
        List<PerformanceViewMapper.ApprovalItemRow> rows =
                performanceViewMapper.findApprovalItems(employeeId);
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
        int updated =
                performanceViewMapper.approvePerformance(
                        employeeId, performanceId, blankToNull(request.comment()));
        if (updated == 0) {
            throw new PerformanceNotFoundException("승인할 성과를 찾을 수 없습니다.");
        }
        if (targetEmployeeId != null) {
            performanceDashboardSummaryService.recalculateCurrentMonth(targetEmployeeId);
        }
    }

    @Transactional
    public void rejectPerformance(
            Long employeeId, Long performanceId, PerformanceApprovalActionRequest request) {
        int updated =
                performanceViewMapper.rejectPerformance(
                        employeeId, performanceId, blankToNull(request.comment()));
        if (updated == 0) {
            throw new PerformanceNotFoundException("반려할 성과를 찾을 수 없습니다.");
        }
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
