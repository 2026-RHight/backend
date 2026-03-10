package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.request.PerformanceApprovalActionRequest;
import com.reverse.performance.internal.dto.response.PerformanceApprovalItemResponse;
import com.reverse.performance.internal.dto.response.PerformanceApprovalResponse;
import com.reverse.performance.internal.exception.PerformanceNotFoundException;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceApprovalService {

    private final PerformanceViewMapper performanceViewMapper;

    public PerformanceApprovalResponse getApprovalItems(Long employeeId) {
        List<PerformanceApprovalItemResponse> allItems =
                performanceViewMapper.findApprovalItems(employeeId);
        List<PerformanceApprovalItemResponse> planItems = new ArrayList<>();
        List<PerformanceApprovalItemResponse> resultItems = new ArrayList<>();

        allItems.stream()
                .sorted(Comparator.comparing(PerformanceApprovalItemResponse::date).reversed())
                .forEach(
                        item -> {
                            if (nvl(item.progress()) > 0) {
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
        int updated =
                performanceViewMapper.approvePerformance(
                        employeeId, performanceId, blankToNull(request.comment()));
        if (updated == 0) {
            throw new PerformanceNotFoundException("승인할 성과를 찾을 수 없습니다.");
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

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
