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
        String payloadJson = "{\"approvalId\":" + approvalId + ",\"payloadSerializeError\":true}";
        try {
            payloadJson = buildFailurePayload(approvalId, effectiveFrom, reason, targetState);
        } catch (Throwable payloadEx) {
            log.error(
                    "HR 승인 이벤트 실패 페이로드 생성 실패. approvalId={}, state={}, secondaryError={}",
                    approvalId,
                    targetState,
                    payloadEx.getMessage(),
                    payloadEx);
        }

        try {
            failedHrEventService.recordFailure(
                    approvalId, targetState, effectiveFrom, reason, payloadJson, failureMessage);
        } catch (Throwable recordEx) {
            log.error(
                    "HR 승인 이벤트 실패 이력 저장 실패. approvalId={}, state={}, secondaryError={}",
                    approvalId,
                    targetState,
                    recordEx.getMessage(),
                    recordEx);
        }

        try {
            adminAlertService.notifyHrEventFailure(approvalId, targetState, reason, failureMessage);
        } catch (Throwable alertEx) {
            log.error(
                    "HR 승인 이벤트 실패 알림 전송 실패. approvalId={}, state={}, secondaryError={}",
                    approvalId,
                    targetState,
                    alertEx.getMessage(),
                    alertEx);
        }

        try {
            if (lastException != null) {
                log.error(
                        "HR 승인 이벤트 최종 실패. approvalId={}, state={}",
                        approvalId,
                        targetState,
                        lastException);
            } else {
                log.error("HR 승인 이벤트 최종 실패. approvalId={}, state={}", approvalId, targetState);
            }
        } catch (Throwable finalLogEx) {
            System.err.println(
                    "HR 승인 이벤트 최종 실패 로그 출력 실패. approvalId="
                            + approvalId
                            + ", state="
                            + targetState
                            + ", secondaryError="
                            + finalLogEx.getMessage());
            if (lastException != null) {
                lastException.printStackTrace(System.err);
            }
            finalLogEx.printStackTrace(System.err);
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
