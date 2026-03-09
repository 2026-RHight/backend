package com.reverse.hr.internal.persistence.row;

public record CareerItemRow(
        Long careerId,
        String companyName,
        String orgName,
        String startDate,
        String endDate,
        Long hrFileId) {}
