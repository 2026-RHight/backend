package com.reverse.hr.internal.application;

import com.reverse.hr.internal.domain.enums.EmployeeState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdminAlertService {

    public void notifyHrEventFailure(
            Long approvalId, EmployeeState targetState, String reason, String failureMessage) {
        log.error(
                "[ADMIN_ALERT] HR 이벤트 반영 실패 - approvalId={}, targetState={}, reason={}, error={}",
                approvalId,
                targetState,
                reason,
                failureMessage);
    }

    public void notifyHrEventRecovered(Long approvalId, EmployeeState targetState) {
        log.info(
                "[ADMIN_ALERT] HR 이벤트 재처리 성공 - approvalId={}, targetState={}",
                approvalId,
                targetState);
    }
}
