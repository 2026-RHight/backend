package com.reverse.hr.internal.persistence.param;

import com.reverse.hr.internal.domain.enums.CertificateRequestStatus;
import com.reverse.hr.internal.domain.enums.CertificateType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequestCreateParam {
    private Long requestId;
    private Long employeeId;
    private CertificateType certificateType;
    private String purpose;
    private String submitTo;
    private CertificateRequestStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime issuedAt;
    private Long hrFileId;
    private String failReason;
}
