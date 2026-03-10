package com.reverse.performance.internal.application;

import com.reverse.performance.internal.domain.Status;
import com.reverse.performance.internal.domain.WorkItem;
import com.reverse.performance.internal.dto.request.PerformanceCreateDTO;
import com.reverse.performance.internal.dto.request.PerformancePersonalRequest;
import com.reverse.performance.internal.dto.request.PerformanceRegistrationRequest;
import com.reverse.performance.internal.dto.request.PerformanceRequest;
import com.reverse.performance.internal.dto.request.PerformanceTeamRequest;
import com.reverse.performance.internal.exception.PerformanceActionNotAllowedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceRegistrationService {

    private final PerformanceService performanceService;

    @Transactional
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
                        Status.ACTIVE,
                        0,
                        resolveDifficultyScore(request.weight()),
                        null,
                        null);

        PerformancePersonalRequest personalRequest =
                workItem == WorkItem.PERSONAL
                        ? new PerformancePersonalRequest(
                                null, blankToNull(request.value()), null, null, null)
                        : null;
        PerformanceTeamRequest teamRequest =
                workItem == WorkItem.TEAM
                        ? new PerformanceTeamRequest(null, request.weight(), null, null)
                        : null;

        performanceService.save(
                employeeId,
                new PerformanceCreateDTO(performanceRequest, personalRequest, teamRequest));
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

    private int resolveDifficultyScore(Integer weight) {
        if (weight == null) {
            return 5;
        }
        return Math.max(1, Math.min(10, Math.round(weight / 10.0f)));
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
}
