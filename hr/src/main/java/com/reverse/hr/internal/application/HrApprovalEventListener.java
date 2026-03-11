package com.reverse.hr.internal.application;

import com.reverse.core.event.ApprovalLeaveEvent;
import com.reverse.core.event.ApprovalRTWEvent;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class HrApprovalEventListener {

    private final HrChangeService hrChangeService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeaveApproved(ApprovalLeaveEvent event) {
        try {
            hrChangeService.enqueueStateChangeEventFromApproval(
                    event.approvalId(),
                    EmployeeState.LEAVE,
                    event.startDate().toLocalDate(),
                    "휴직 승인 반영",
                    event.reason());
        } catch (Exception ex) {
            log.error("휴직 승인 이벤트 반영 실패. approvalId={}", event.approvalId(), ex);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRtwApproved(ApprovalRTWEvent event) {
        try {
            hrChangeService.enqueueStateChangeEventFromApproval(
                    event.approvalId(),
                    EmployeeState.WORK,
                    event.rtwDate(),
                    "복직 승인 반영",
                    event.reason());
        } catch (Exception ex) {
            log.error("복직 승인 이벤트 반영 실패. approvalId={}", event.approvalId(), ex);
        }
    }
}
