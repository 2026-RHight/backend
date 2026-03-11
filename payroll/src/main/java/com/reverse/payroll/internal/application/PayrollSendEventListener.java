package com.reverse.payroll.internal.application;

import com.reverse.payroll.internal.event.PayrollPayslipSendRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayrollSendEventListener {

    private final PayrollService payrollService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePayslipSendRequested(PayrollPayslipSendRequestedEvent event) {

        try {
            payrollService.processPayslipSendRequest(event.ledgerId());
        } catch (RuntimeException e) {
            log.error("급여 명세서 발송 요청 처리 중 오류 발생 - ledgerId: {}", event.ledgerId(), e);
            throw e;
        }
    }
}
