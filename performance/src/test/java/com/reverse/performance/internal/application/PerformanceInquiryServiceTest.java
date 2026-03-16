package com.reverse.performance.internal.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.reverse.core.service.S3StorageService;
import com.reverse.performance.internal.dto.request.PerformanceResultUpdateRequest;
import com.reverse.performance.internal.exception.PerformanceActionNotAllowedException;
import com.reverse.performance.internal.exception.PerformanceNotFoundException;
import com.reverse.performance.internal.persistence.AttachmentMapper;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PerformanceInquiryServiceTest {

    @Mock private PerformanceHrMemberResolver performanceHrMemberResolver;

    @Mock private PerformanceViewMapper performanceViewMapper;

    @Mock private AttachmentMapper attachmentMapper;

    @Mock private S3StorageService s3StorageService;

    @InjectMocks private PerformanceInquiryService performanceInquiryService;

    @Test
    void updateResult_updatesTeamPerformanceAndRecalculatesSummary() {
        Long employeeId = 20L;
        Long performanceId = 227L;
        PerformanceResultUpdateRequest request =
                new PerformanceResultUpdateRequest(80, "팀 성과 요약", "특이점", null, null);

        when(performanceViewMapper.countOwnedPerformance(employeeId, performanceId)).thenReturn(1);
        when(performanceViewMapper.updatePerformanceResult(performanceId, 80, "팀 성과 요약"))
                .thenReturn(1);
        when(performanceViewMapper.updatePersonalResult(performanceId, "팀 성과 요약", null, null))
                .thenReturn(0);
        when(performanceViewMapper.updateTeamResult(performanceId, "팀 성과 요약", "특이점")).thenReturn(1);

        performanceInquiryService.updateResult(employeeId, performanceId, request, List.of());

        verify(performanceViewMapper).countOwnedPerformance(employeeId, performanceId);
        verify(performanceViewMapper).updatePerformanceResult(performanceId, 80, "팀 성과 요약");
        verify(performanceViewMapper).updatePersonalResult(performanceId, "팀 성과 요약", null, null);
        verify(performanceViewMapper).updateTeamResult(performanceId, "팀 성과 요약", "특이점");
    }

    @Test
    void updateResult_throwsWhenNoDetailRowExists() {
        Long employeeId = 20L;
        Long performanceId = 227L;
        PerformanceResultUpdateRequest request =
                new PerformanceResultUpdateRequest(80, "개인 성과 요약", null, "성장", "개선");

        when(performanceViewMapper.countOwnedPerformance(employeeId, performanceId)).thenReturn(1);
        when(performanceViewMapper.updatePerformanceResult(performanceId, 80, "개인 성과 요약"))
                .thenReturn(1);
        when(performanceViewMapper.updatePersonalResult(performanceId, "개인 성과 요약", "성장", "개선"))
                .thenReturn(0);
        when(performanceViewMapper.updateTeamResult(performanceId, "개인 성과 요약", null)).thenReturn(0);

        assertThrows(
                PerformanceActionNotAllowedException.class,
                () ->
                        performanceInquiryService.updateResult(
                                employeeId, performanceId, request, List.of()));
    }

    @Test
    void updateResult_throwsWhenActivePerformanceWasNotUpdated() {
        Long employeeId = 20L;
        Long performanceId = 227L;
        PerformanceResultUpdateRequest request =
                new PerformanceResultUpdateRequest(80, "개인 성과 요약", null, "성장", "개선");

        when(performanceViewMapper.countOwnedPerformance(employeeId, performanceId)).thenReturn(1);
        when(performanceViewMapper.updatePerformanceResult(performanceId, 80, "개인 성과 요약"))
                .thenReturn(0);

        assertThrows(
                PerformanceNotFoundException.class,
                () ->
                        performanceInquiryService.updateResult(
                                employeeId, performanceId, request, List.of()));

        verify(performanceViewMapper, never())
                .updatePersonalResult(performanceId, "개인 성과 요약", "성장", "개선");
    }
}
