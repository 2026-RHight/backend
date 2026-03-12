package com.reverse.performance.internal.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.reverse.performance.internal.dto.request.PerformanceRegistrationRequest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;

@ExtendWith(MockitoExtension.class)
class PerformanceRegistrationServiceTest {

    @Mock private PerformanceService performanceService;

    @InjectMocks private PerformanceRegistrationService performanceRegistrationService;

    @Test
    void register_retriesWhenLockCannotBeAcquired() {
        Long employeeId = 1L;
        PerformanceRegistrationRequest request =
                new PerformanceRegistrationRequest(
                        "individual",
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 3, 31),
                        5,
                        "title",
                        "coreTask",
                        "content",
                        null);

        doThrow(new CannotAcquireLockException("lock timeout"))
                .doThrow(new CannotAcquireLockException("lock timeout"))
                .doNothing()
                .when(performanceService)
                .save(eq(employeeId), any());

        performanceRegistrationService.register(employeeId, request);

        verify(performanceService, times(3)).save(eq(employeeId), any());
    }

    @Test
    void register_throwsWhenLockCannotBeAcquiredAfterMaxRetries() {
        Long employeeId = 1L;
        PerformanceRegistrationRequest request =
                new PerformanceRegistrationRequest(
                        "team",
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 3, 31),
                        4,
                        "title",
                        "coreTask",
                        "content",
                        null);

        doThrow(new CannotAcquireLockException("lock timeout"))
                .when(performanceService)
                .save(eq(employeeId), any());

        assertThrows(
                CannotAcquireLockException.class,
                () -> performanceRegistrationService.register(employeeId, request));

        verify(performanceService, times(3)).save(eq(employeeId), any());
    }
}
