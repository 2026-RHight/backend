package com.reverse.hr.internal.persistence.row;

import java.time.LocalDate;

public record BasicInfoRow(
        String employeeNum,
        String employeeName,
        String email,
        String phone,
        String extensionNum,
        LocalDate birthDate,
        String address,
        String residentNumberEnc,
        String bankName,
        String accountNumberEnc,
        String profileFileUrl) {}
