package com.reverse.hr.internal.persistence.row;

public record LoginUserRow(
        Long employeeId,
        String employeeNum,
        String password,
        Boolean initialState
) {
}
