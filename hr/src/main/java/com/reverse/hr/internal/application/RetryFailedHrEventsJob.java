package com.reverse.hr.internal.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryFailedHrEventsJob {

    private final FailedHrEventService failedHrEventService;

    @Scheduled(cron = "${hr.failed-event.retry.cron:0 */10 * * * *}", zone = "Asia/Seoul")
    public void run() {
        int processed = failedHrEventService.retryFailedEvents();
        if (processed > 0) {
            log.info("재처리된 실패 HR 이벤트 수: {}", processed);
        }
    }
}
