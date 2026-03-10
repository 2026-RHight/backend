package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.CertificateRequestStatus;
import com.reverse.hr.internal.domain.enums.CertificateType;

public record CreateCertificateRequestResponseDTO(
        Long requestId,
        CertificateType certificateType,
        CertificateRequestStatus status,
        String issuedAt,
        Long hrFileId) {}
