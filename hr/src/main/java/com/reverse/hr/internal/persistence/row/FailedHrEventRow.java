package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.EmployeeState;
import java.time.LocalDate;

public record FailedHrEventRow(
        Long failedEventId,
        Long sourceApprovalId,
        EmployeeState targetEmployeeState,
        LocalDate effectiveFrom,
        String reason,
        String payloadJson,
        int retryCount) {}
