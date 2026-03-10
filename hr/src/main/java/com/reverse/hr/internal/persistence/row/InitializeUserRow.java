package com.reverse.hr.internal.persistence.row;

public record InitializeUserRow(
        Long employeeId, String employeeNum, String residentNumberHash, String email) {}
