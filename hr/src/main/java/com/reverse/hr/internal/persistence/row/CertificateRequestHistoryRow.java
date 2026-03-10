package com.reverse.hr.internal.persistence.row;

public record CertificateRequestHistoryRow(
        Long requestId, String certificateType, String status, String issuedDate) {}
