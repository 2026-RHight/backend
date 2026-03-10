package com.reverse.hr.internal.dto.request;

import com.reverse.hr.internal.domain.enums.CertificateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCertificateRequestDTO(
        @NotNull(message = "증명서 종류는 필수입니다.") CertificateType certificateType,
        @NotBlank(message = "제출처는 필수입니다.") String submitTo,
        @NotBlank(message = "용도는 필수입니다.") String purpose) {}
