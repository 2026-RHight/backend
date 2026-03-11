package com.reverse.hr.internal.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reverse.core.event.ApprovalLeaveEvent;
import com.reverse.core.event.ApprovalRTWEvent;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class HrApprovalEventListener {

    private static final int INITIAL_RETRY_ATTEMPTS = 3;

    private final HrChangeService hrChangeService;
    private final FailedHrEventService failedHrEventService;
    private final AdminAlertService adminAlertService;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeaveApproved(ApprovalLeaveEvent event) {
        LocalDate effectiveFrom = event.startDate().toLocalDate();
        processWithRetry(
                event.approvalId(), EmployeeState.LEAVE, effectiveFrom, "휴직 승인 반영", event.reason());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRtwApproved(ApprovalRTWEvent event) {
        processWithRetry(
                event.approvalId(),
                EmployeeState.WORK,
                event.rtwDate(),
                "복직 승인 반영",
                event.reason());
    }

    private void processWithRetry(
            Long approvalId,
            EmployeeState targetState,
            LocalDate effectiveFrom,
            String titlePrefix,
            String reason) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= INITIAL_RETRY_ATTEMPTS; attempt++) {
            try {
                hrChangeService.enqueueStateChangeEventFromApproval(
                        approvalId, targetState, effectiveFrom, titlePrefix, reason);
                if (attempt > 1) {
                    log.info(
                            "HR 승인 이벤트 재시도 성공. approvalId={}, state={}, attempt={}",
                            approvalId,
                            targetState,
                            attempt);
                }
                return;
            } catch (Exception ex) {
                lastException = ex;
                log.warn(
                        "HR 승인 이벤트 반영 실패. approvalId={}, state={}, attempt={}",
                        approvalId,
                        targetState,
                        attempt,
                        ex);
            }
        }

        String failureMessage = lastException == null ? "unknown" : lastException.getMessage();
        String payloadJson = buildFailurePayload(approvalId, effectiveFrom, reason, targetState);
        failedHrEventService.recordFailure(
                approvalId, targetState, effectiveFrom, reason, payloadJson, failureMessage);
        adminAlertService.notifyHrEventFailure(approvalId, targetState, reason, failureMessage);

        if (lastException != null) {
            log.error(
                    "HR 승인 이벤트 최종 실패. approvalId={}, state={}",
                    approvalId,
                    targetState,
                    lastException);
        }
    }

    private String buildFailurePayload(
            Long approvalId, LocalDate effectiveFrom, String reason, EmployeeState targetState) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("approvalId", approvalId);
        payload.put("effectiveFrom", effectiveFrom);
        payload.put("reason", reason);
        payload.put("targetEmployeeState", targetState);
        payload.put("failedAt", OffsetDateTime.now().toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "{\"approvalId\":" + approvalId + ",\"payloadSerializeError\":true}";
        }
    }
}
