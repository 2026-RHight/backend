package com.reverse.hr.internal.application;

import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.persistence.FailedHrEventRepository;
import com.reverse.hr.internal.persistence.row.FailedHrEventRow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FailedHrEventService {

    private static final String RETRY_LOCK_NAME = "failed_hr_event_retry_job_lock";

    @Value("${hr.failed-event.retry.max-count:10}")
    private int maxRetryCount;

    @Value("${hr.failed-event.retry.batch-size:100}")
    private int retryBatchSize;

    private final FailedHrEventRepository failedHrEventRepository;
    private final HrChangeService hrChangeService;
    private final AdminAlertService adminAlertService;

    @Transactional
    public void recordFailure(
            Long approvalId,
            EmployeeState targetState,
            java.time.LocalDate effectiveFrom,
            String reason,
            String payloadJson,
            String failureMessage) {
        failedHrEventRepository.upsertFailedEvent(
                approvalId, targetState, effectiveFrom, reason, payloadJson, failureMessage);
    }

    public int retryFailedEvents() {
        Integer lock = failedHrEventRepository.acquireRetryLock(RETRY_LOCK_NAME);
        if (lock == null || lock != 1) {
            return 0;
        }

        try {
            return retryFailedEventsInternal();
        } finally {
            failedHrEventRepository.releaseRetryLock(RETRY_LOCK_NAME);
        }
    }

    private int retryFailedEventsInternal() {
        int processed = 0;
        int safeBatchSize = Math.max(1, retryBatchSize);
        int safeMaxRetry = Math.max(1, maxRetryCount);
        List<FailedHrEventRow> retryTargets =
                failedHrEventRepository.findRetryTargets(safeBatchSize, safeMaxRetry);

        for (FailedHrEventRow failedEvent : retryTargets) {
            if (failedHrEventRepository.markRetrying(failedEvent.failedEventId()) != 1) {
                continue;
            }

            try {
                hrChangeService.enqueueStateChangeEventFromApproval(
                        failedEvent.sourceApprovalId(),
                        failedEvent.targetEmployeeState(),
                        failedEvent.effectiveFrom(),
                        failedEvent.targetEmployeeState() == EmployeeState.LEAVE
                                ? "휴직 승인 반영(재처리)"
                                : "복직 승인 반영(재처리)",
                        failedEvent.reason());

                failedHrEventRepository.markResolved(failedEvent.failedEventId());
                adminAlertService.notifyHrEventRecovered(
                        failedEvent.sourceApprovalId(), failedEvent.targetEmployeeState());
                processed++;
            } catch (Exception ex) {
                failedHrEventRepository.markRetryFailure(
                        failedEvent.failedEventId(), ex.getMessage(), safeMaxRetry);
                adminAlertService.notifyHrEventFailure(
                        failedEvent.sourceApprovalId(),
                        failedEvent.targetEmployeeState(),
                        failedEvent.reason(),
                        ex.getMessage());
                log.warn(
                        "HR 실패 이벤트 재처리 실패. failedEventId={}, approvalId={}",
                        failedEvent.failedEventId(),
                        failedEvent.sourceApprovalId(),
                        ex);
            }
        }
        return processed;
    }
}
