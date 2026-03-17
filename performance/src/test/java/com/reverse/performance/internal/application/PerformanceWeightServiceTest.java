package com.reverse.performance.internal.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.reverse.performance.internal.dto.request.PerformanceWeightItemRequest;
import com.reverse.performance.internal.dto.request.PerformanceWeightUpsertRequest;
import com.reverse.performance.internal.persistence.MonthlyPerformanceMapper;
import com.reverse.performance.internal.persistence.PerformanceWeightMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PerformanceWeightServiceTest {

    @Mock private PerformanceWeightMapper performanceWeightMapper;

    @Mock private MonthlyPerformanceMapper monthlyPerformanceMapper;

    @Mock private PerformanceDashboardSummaryService performanceDashboardSummaryService;

    @InjectMocks private PerformanceWeightService performanceWeightService;

    @Test
    void upsertWeights_updatesWeightsAndRefreshesScores() {
        PerformanceWeightUpsertRequest request =
                new PerformanceWeightUpsertRequest(
                        List.of(new PerformanceWeightItemRequest(9304L, 60, 40)));

        when(performanceWeightMapper.countTeamOrganizations(List.of(9304L))).thenReturn(1);
        when(performanceWeightMapper.updateWeight(9304L, 60, 40)).thenReturn(1);
        when(performanceWeightMapper.findEmployeeIdsByOrgIds(List.of(9304L)))
                .thenReturn(List.of(1001L, 1002L));
        when(monthlyPerformanceMapper.calculateMonthlyScore(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(87);
        when(performanceWeightMapper.findAllTeamWeights())
                .thenReturn(
                        List.of(
                                new PerformanceWeightMapper.PerformanceWeightRow(
                                        9304L, 60, 40, null)));

        performanceWeightService.upsertWeights(request);

        verify(performanceWeightMapper).updateWeight(9304L, 60, 40);
        verify(monthlyPerformanceMapper)
                .calculateMonthlyScore(
                        org.mockito.ArgumentMatchers.eq(1001L),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt());
        verify(monthlyPerformanceMapper)
                .calculateMonthlyScore(
                        org.mockito.ArgumentMatchers.eq(1002L),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt());
        verify(monthlyPerformanceMapper)
                .upsertMonthlyScore(
                        org.mockito.ArgumentMatchers.eq(1001L),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.eq(87));
        verify(monthlyPerformanceMapper)
                .upsertMonthlyScore(
                        org.mockito.ArgumentMatchers.eq(1002L),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.eq(87));
        verify(performanceDashboardSummaryService).recalculateCurrentMonth(1001L);
        verify(performanceDashboardSummaryService).recalculateCurrentMonth(1002L);
    }

    @Test
    void upsertWeights_throwsWhenWeightSumIsNotHundred() {
        PerformanceWeightUpsertRequest request =
                new PerformanceWeightUpsertRequest(
                        List.of(new PerformanceWeightItemRequest(9304L, 70, 20)));

        assertThrows(
                ResponseStatusException.class,
                () -> performanceWeightService.upsertWeights(request));

        verify(performanceWeightMapper, never())
                .updateWeight(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt());
        verify(performanceWeightMapper, never())
                .insertWeight(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void upsertWeights_insertsWhenNoRowExists() {
        PerformanceWeightUpsertRequest request =
                new PerformanceWeightUpsertRequest(
                        List.of(new PerformanceWeightItemRequest(9304L, 55, 45)));

        when(performanceWeightMapper.countTeamOrganizations(List.of(9304L))).thenReturn(1);
        when(performanceWeightMapper.updateWeight(9304L, 55, 45)).thenReturn(0);
        when(performanceWeightMapper.insertWeight(9304L, 55, 45)).thenReturn(1);
        when(performanceWeightMapper.findEmployeeIdsByOrgIds(List.of(9304L))).thenReturn(List.of());
        when(performanceWeightMapper.findAllTeamWeights())
                .thenReturn(
                        List.of(
                                new PerformanceWeightMapper.PerformanceWeightRow(
                                        9304L, 55, 45, null)));

        performanceWeightService.upsertWeights(request);

        verify(performanceWeightMapper).updateWeight(9304L, 55, 45);
        verify(performanceWeightMapper).insertWeight(9304L, 55, 45);
    }

    @Test
    void upsertWeights_retriesUpdateWhenInsertConflicts() {
        PerformanceWeightUpsertRequest request =
                new PerformanceWeightUpsertRequest(
                        List.of(new PerformanceWeightItemRequest(9304L, 65, 35)));

        when(performanceWeightMapper.countTeamOrganizations(List.of(9304L))).thenReturn(1);
        when(performanceWeightMapper.updateWeight(9304L, 65, 35)).thenReturn(0, 1);
        when(performanceWeightMapper.insertWeight(9304L, 65, 35))
                .thenThrow(new DuplicateKeyException("duplicate"));
        when(performanceWeightMapper.findEmployeeIdsByOrgIds(List.of(9304L))).thenReturn(List.of());
        when(performanceWeightMapper.findAllTeamWeights())
                .thenReturn(
                        List.of(
                                new PerformanceWeightMapper.PerformanceWeightRow(
                                        9304L, 65, 35, null)));

        performanceWeightService.upsertWeights(request);

        verify(performanceWeightMapper).insertWeight(9304L, 65, 35);
        verify(performanceWeightMapper, org.mockito.Mockito.times(2)).updateWeight(9304L, 65, 35);
    }
}
