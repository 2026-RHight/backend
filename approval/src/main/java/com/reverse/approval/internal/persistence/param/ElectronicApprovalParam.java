package com.reverse.approval.internal.persistence.param;

import com.reverse.approval.internal.domain.enums.ApprovalStatus;
import com.reverse.approval.internal.domain.enums.DocType;
import com.reverse.approval.internal.dto.request.DraftApproval;
import com.reverse.hr.dto.EmployeeProfileDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class ElectronicApprovalParam {
    @Setter
    private Long approvalId;
    private final String title;
    private final DocType docType;
    private final ApprovalStatus approvalStatus;
    private final LocalDateTime draftDate;
    private final Long drafterId;
    private final String drafterName;
    private final String departmentName;

    @Builder
    public ElectronicApprovalParam(String title, DocType docType, ApprovalStatus approvalStatus, LocalDateTime draftDate, Long drafterId, String drafterName, String departmentName) {
        this.title = title;
        this.docType = docType;
        this.approvalStatus = approvalStatus;
        this.draftDate = draftDate;
        this.drafterId = drafterId;
        this.drafterName = drafterName;
        this.departmentName = departmentName;
    }

    public static ElectronicApprovalParam from(
            DraftApproval dto,
            EmployeeProfileDTO profile,
            ApprovalStatus status
    ) {
        return ElectronicApprovalParam.builder()
                .title(dto.getTitle())
                .docType(dto.getDocType())
                .approvalStatus(status)
                .draftDate(LocalDateTime.now())
                .drafterId(profile.employeeId())
                .drafterName(profile.employeeName())
                .departmentName(profile.orgName() == null ? "미지정" : profile.orgName())
                .build();
    }
}
