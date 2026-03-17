package com.reverse.performance.internal.application;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.reverse.performance.internal.dto.request.PerformanceWeightItemRequest;
import com.reverse.performance.internal.dto.request.PerformanceWeightUpsertRequest;
import com.reverse.performance.internal.dto.response.PerformanceWeightResponse;
import com.reverse.performance.internal.persistence.MonthlyPerformanceMapper;
import com.reverse.performance.internal.persistence.PerformanceWeightMapper;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceWeightService {

    private final PerformanceWeightMapper performanceWeightMapper;
    private final MonthlyPerformanceMapper monthlyPerformanceMapper;
    private final PerformanceDashboardSummaryService performanceDashboardSummaryService;

    public List<PerformanceWeightResponse> getWeights() {
        return performanceWeightMapper.findAllTeamWeights().stream()
                .map(
                        row ->
                                new PerformanceWeightResponse(
                                        row.orgId(),
                                        nvl(row.personalWeightRate(), 50),
                                        nvl(row.teamWeightRate(), 50),
                                        row.updatedAt()))
                .toList();
    }

    @Transactional
    public List<PerformanceWeightResponse> upsertWeights(PerformanceWeightUpsertRequest request) {
        List<PerformanceWeightItemRequest> items = normalize(request);
        if (items.isEmpty()) {
            return getWeights();
        }

        List<Long> orgIds = distinctOrgIds(items);
        Integer validTeamCount = performanceWeightMapper.countTeamOrganizations(orgIds);
        if (!Objects.equals(validTeamCount, orgIds.size())) {
            throw new ResponseStatusException(BAD_REQUEST, "팀 조직만 성과 반영 비율을 저장할 수 있습니다.");
        }

        for (PerformanceWeightItemRequest item : items) {
            saveWeight(item);
        }

        refreshCurrentMonthScores(orgIds);
        return getWeights();
    }

    private List<PerformanceWeightItemRequest> normalize(PerformanceWeightUpsertRequest request) {
        if (request == null || request.weights() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "weights는 필수입니다.");
        }

        return request.weights().stream().filter(Objects::nonNull).map(this::validateItem).toList();
    }

    private PerformanceWeightItemRequest validateItem(PerformanceWeightItemRequest item) {
        if (item.orgId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "orgId는 필수입니다.");
        }
        Integer personalWeightRate = validateRate("personalWeightRate", item.personalWeightRate());
        Integer teamWeightRate = validateRate("teamWeightRate", item.teamWeightRate());
        if (personalWeightRate + teamWeightRate != 100) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "개인 성과 반영 비율과 팀 성과 반영 비율의 합은 100이어야 합니다.");
        }
        return new PerformanceWeightItemRequest(item.orgId(), personalWeightRate, teamWeightRate);
    }

    private Integer validateRate(String fieldName, Integer value) {
        if (value == null) {
            throw new ResponseStatusException(BAD_REQUEST, fieldName + "는 필수입니다.");
        }
        if (value < 0 || value > 100) {
            throw new ResponseStatusException(BAD_REQUEST, fieldName + "는 0 이상 100 이하여야 합니다.");
        }
        return value;
    }

    private List<Long> distinctOrgIds(List<PerformanceWeightItemRequest> items) {
        Set<Long> orgIds = new LinkedHashSet<>();
        for (PerformanceWeightItemRequest item : items) {
            if (!orgIds.add(item.orgId())) {
                throw new ResponseStatusException(BAD_REQUEST, "중복된 orgId가 포함되어 있습니다.");
            }
        }
        return List.copyOf(orgIds);
    }

    private void saveWeight(PerformanceWeightItemRequest item) {
        int updated =
                performanceWeightMapper.updateWeight(
                        item.orgId(), item.personalWeightRate(), item.teamWeightRate());
        if (updated > 0) {
            return;
        }

        try {
            performanceWeightMapper.insertWeight(
                    item.orgId(), item.personalWeightRate(), item.teamWeightRate());
        } catch (DuplicateKeyException ex) {
            int retriedUpdated =
                    performanceWeightMapper.updateWeight(
                            item.orgId(), item.personalWeightRate(), item.teamWeightRate());
            if (retriedUpdated == 0) {
                throw ex;
            }
        }
    }

    private void refreshCurrentMonthScores(List<Long> orgIds) {
        List<Long> employeeIds = performanceWeightMapper.findEmployeeIdsByOrgIds(orgIds);
        if (employeeIds == null || employeeIds.isEmpty()) {
            return;
        }

        YearMonth currentMonth = YearMonth.now();
        for (Long employeeId : employeeIds.stream().filter(Objects::nonNull).distinct().toList()) {
            Integer score =
                    monthlyPerformanceMapper.calculateMonthlyScore(
                            employeeId, currentMonth.getYear(), currentMonth.getMonthValue());
            if (score != null) {
                monthlyPerformanceMapper.upsertMonthlyScore(
                        employeeId, currentMonth.getYear(), currentMonth.getMonthValue(), score);
            }
            performanceDashboardSummaryService.recalculateCurrentMonth(employeeId);
        }
    }

    private int nvl(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
