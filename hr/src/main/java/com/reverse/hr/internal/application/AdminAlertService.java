package com.reverse.hr.internal.application;

import com.reverse.core.event.EmailSendEvent;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAlertService {

    @Value("${app.mail.alert-to:${app.mail.from:}}")
    private String alertTo;

    private final ApplicationEventPublisher eventPublisher;

    public void notifyHrEventFailure(
            Long approvalId, EmployeeState targetState, String reason, String failureMessage) {
        log.error(
                "[ADMIN_ALERT] HR 이벤트 반영 실패 - approvalId={}, targetState={}",
                approvalId,
                targetState);

        if (!StringUtils.hasText(alertTo)) {
            return;
        }

        String subject =
                "[RHight][HR_ALERT] HR 이벤트 반영 실패 - approvalId="
                        + approvalId
                        + ", state="
                        + targetState;
        String body =
                "<h3>HR 이벤트 반영 실패</h3>"
                        + "<p><b>approvalId:</b> "
                        + approvalId
                        + "</p>"
                        + "<p><b>targetState:</b> "
                        + targetState
                        + "</p>"
                        + "<p><b>reason:</b> "
                        + escapeHtml(reason)
                        + "</p>"
                        + "<p><b>error:</b> "
                        + escapeHtml(failureMessage)
                        + "</p>"
                        + "<p><b>occurredAt:</b> "
                        + OffsetDateTime.now()
                        + "</p>";
        eventPublisher.publishEvent(new EmailSendEvent(alertTo, subject, body));
    }

    public void notifyHrEventRecovered(Long approvalId, EmployeeState targetState) {
        log.info(
                "[ADMIN_ALERT] HR 이벤트 재처리 성공 - approvalId={}, targetState={}",
                approvalId,
                targetState);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "-";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
