package com.reverse.approval.internal.persistence.row;

import java.time.LocalDate;
import java.time.LocalTime;

public record OvertimeDetailRow(
        LocalDate workDate, LocalTime startTime, LocalTime endTime, String reason) {}
