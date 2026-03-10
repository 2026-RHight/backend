package com.reverse.hr.internal.dto.response;

public record CertificateRequestHistoryResponseDTO(
        Long requestId,
        String certificateType,
        String certificateName,
        String issuedDate,
        String status,
        String statusName) {}
