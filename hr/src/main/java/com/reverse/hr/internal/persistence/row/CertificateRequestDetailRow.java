package com.reverse.hr.internal.persistence.row;

import java.time.LocalDateTime;

public record CertificateRequestDetailRow(
        Long requestId,
        String certificateType,
        String purpose,
        String submitTo,
        String status,
        LocalDateTime requestedAt,
        LocalDateTime issuedAt) {}
