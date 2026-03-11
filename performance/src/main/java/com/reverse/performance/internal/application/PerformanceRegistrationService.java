package com.reverse.performance.internal.application;

import com.reverse.core.exception.BadRequestException;
import com.reverse.performance.internal.domain.Status;
import com.reverse.performance.internal.domain.WorkItem;
import com.reverse.performance.internal.dto.request.PerformanceCreateDTO;
import com.reverse.performance.internal.dto.request.PerformancePersonalRequest;
import com.reverse.performance.internal.dto.request.PerformanceRegistrationRequest;
import com.reverse.performance.internal.dto.request.PerformanceRequest;
import com.reverse.performance.internal.dto.request.PerformanceTeamRequest;
import com.reverse.performance.internal.exception.PerformanceActionNotAllowedException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PerformanceRegistrationService {

    private static final int MAX_LOCK_RETRIES = 3;
    private static final long LOCK_RETRY_DELAY_MILLIS = 150L;

    private final PerformanceService performanceService;

    public void register(Long employeeId, PerformanceRegistrationRequest request) {
        if (request == null) {
            throw new PerformanceActionNotAllowedException("등록 요청이 비어 있습니다.");
        }

        WorkItem workItem = resolveWorkItem(request.type());
        PerformanceRequest performanceRequest =
                new PerformanceRequest(
                        null,
                        request.title(),
                        workItem,
                        request.startDate(),
                        request.endDate(),
                        buildWorkDetail(request.coreTask(), request.content()),
                        Status.WAITING,
                        0,
                        resolveDifficultyScore(request.difficultyScore()),
                        null,
                        null);

        PerformancePersonalRequest personalRequest =
                workItem == WorkItem.PERSONAL
                        ? new PerformancePersonalRequest(
                                null, blankToNull(request.value()), null, null, null)
                        : null;
        PerformanceTeamRequest teamRequest =
                workItem == WorkItem.TEAM
                        ? new PerformanceTeamRequest(null, null, null, null)
                        : null;

        PerformanceCreateDTO dto =
                new PerformanceCreateDTO(performanceRequest, personalRequest, teamRequest);

        runWithLockRetry(employeeId, dto);
    }

    private WorkItem resolveWorkItem(String type) {
        if (type == null || type.isBlank() || type.equalsIgnoreCase("individual")) {
            return WorkItem.PERSONAL;
        }
        if (type.equalsIgnoreCase("team")) {
            return WorkItem.TEAM;
        }
        throw new PerformanceActionNotAllowedException("지원하지 않는 성과 유형입니다.");
    }

    private int resolveDifficultyScore(Integer difficultyScore) {
        if (difficultyScore == null) {
            throw new BadRequestException("difficultyScore는 필수입니다.");
        }
        if (difficultyScore < 1 || difficultyScore > 5) {
            throw new BadRequestException("difficultyScore는 1 이상 5 이하여야 합니다.");
        }
        return difficultyScore;
    }

    private String buildWorkDetail(String coreTask, String content) {
        String title = blankToNull(coreTask);
        String detail = blankToNull(content);
        if (title == null) {
            return detail;
        }
        if (detail == null) {
            return title;
        }
        return title + "\n" + detail;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void runWithLockRetry(Long employeeId, PerformanceCreateDTO dto) {
        for (int attempt = 1; attempt <= MAX_LOCK_RETRIES; attempt++) {
            try {
                performanceService.save(employeeId, dto);
                return;
            } catch (CannotAcquireLockException | DeadlockLoserDataAccessException ex) {
                if (attempt == MAX_LOCK_RETRIES) {
                    throw ex;
                }
                sleepBeforeRetry();
            }
        }
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(LOCK_RETRY_DELAY_MILLIS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 재시도 대기 중 인터럽트가 발생했습니다.", ex);
        }
    }
}
