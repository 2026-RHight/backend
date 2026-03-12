package com.reverse.hr.internal.persistence.row;

import java.time.LocalDate;

public record LoginUserRow(
        Long employeeId,
        String employeeNum,
        String password,
        Boolean initialState,
        LocalDate hireDate) {}
