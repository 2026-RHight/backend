package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.persistence.ApprovalFlexibleQueryMapper;
import com.reverse.attendance.internal.persistence.row.FlexibleApprovalHeaderRow;
import com.reverse.core.event.ApprovalFlexibleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceApprovalEventListener {

    private final ApprovalFlexibleQueryMapper approvalFlexibleQueryMapper;
    private final WeeklyWorkScheduleService weeklyWorkScheduleService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFlexibleApproved(ApprovalFlexibleEvent event) {
        FlexibleApprovalHeaderRow header =
                approvalFlexibleQueryMapper
                        .findFlexibleApprovalHeaderByApprovalId(event.approvalId())
                        .orElseThrow(
                                () ->
                                        new com.reverse.core.exception.NotFoundException(
                                                "승인된 유연근무 기안 정보를 찾을 수 없습니다."));

        weeklyWorkScheduleService.syncApprovedScheduleFromApproval(
                event.approvalId(),
                header.employeeId(),
                event.startDate(),
                event.endDate(),
                event.reason(),
                header.scheduleTitle());

        log.info("유연근무 승인 이벤트를 attendance 스케줄로 반영했습니다. approvalId={}", event.approvalId());
    }
}
