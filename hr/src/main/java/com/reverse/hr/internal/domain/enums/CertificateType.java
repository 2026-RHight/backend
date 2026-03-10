package com.reverse.hr.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CertificateType {
    EMPLOYMENT_KO("재직 증명서");

    private final String description;
}
