package com.reverse.approval.internal.application;

import com.reverse.approval.ApprovalFacade;
import com.reverse.approval.internal.domain.enums.ApprovalStatus;
import com.reverse.approval.internal.dto.request.ApprovalLineRequest;
import com.reverse.approval.internal.dto.request.DraftApproval;
import com.reverse.approval.internal.dto.request.RecipientLineRequest;
import com.reverse.approval.internal.dto.request.ReferenceLineRequest;
import com.reverse.approval.internal.dto.response.DownloadedApprovalFile;
import com.reverse.approval.internal.exception.ApprovalNotFoundException;
import com.reverse.approval.internal.exception.AttachmentNotFoundException;
import com.reverse.approval.internal.exception.FormRequiredException;
import com.reverse.approval.internal.persistence.ApprovalAttachmentMapper;
import com.reverse.approval.internal.persistence.ApprovalLineMapper;
import com.reverse.approval.internal.persistence.ApprovalMapper;
import com.reverse.approval.internal.persistence.BusinessTripDetailMapper;
import com.reverse.approval.internal.persistence.FlexibleWorkDetailMapper;
import com.reverse.approval.internal.persistence.LeaveDetailMapper;
import com.reverse.approval.internal.persistence.OvertimeDetailMapper;
import com.reverse.approval.internal.persistence.RTWDetailMapper;
import com.reverse.approval.internal.persistence.RecipientLineMapper;
import com.reverse.approval.internal.persistence.ReferenceLineMapper;
import com.reverse.approval.internal.persistence.VacationDetailMapper;
import com.reverse.approval.internal.persistence.param.ApprovalAttachmentParam;
import com.reverse.approval.internal.persistence.param.ApprovalLineParam;
import com.reverse.approval.internal.persistence.param.BusinessTripDetailParam;
import com.reverse.approval.internal.persistence.param.ElectronicApprovalParam;
import com.reverse.approval.internal.persistence.param.FlexibleWorkParam;
import com.reverse.approval.internal.persistence.param.LeaveDetailParam;
import com.reverse.approval.internal.persistence.param.OvertimeDetailParam;
import com.reverse.approval.internal.persistence.param.RTWDetailParam;
import com.reverse.approval.internal.persistence.param.RecipientLineParam;
import com.reverse.approval.internal.persistence.param.ReferenceLineParam;
import com.reverse.approval.internal.persistence.param.VacationDetailParam;
import com.reverse.approval.internal.persistence.row.ApprovalAttachmentRow;
import com.reverse.core.exception.ForbiddenException;
import com.reverse.hr.HrFacade;
import com.reverse.hr.dto.EmployeeProfileDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApprovalService implements ApprovalFacade {
    private final HrFacade hrFacade;
    private final ApprovalLineMapper approvalLineMapper;
    private final ApprovalMapper approvalMapper;
    private final VacationDetailMapper vacationMapper;
    private final BusinessTripDetailMapper businessTripMapper;
    private final FlexibleWorkDetailMapper flexibleWorkMapper;
    private final LeaveDetailMapper leaveMapper;
    private final OvertimeDetailMapper overtimeMapper;
    private final RTWDetailMapper rtwMapper;
    private final ReferenceLineMapper referenceLineMapper;
    private final RecipientLineMapper recipientLineMapper;
    private final ApprovalFileService approvalFileService;
    private final ApprovalAttachmentMapper approvalAttachmentMapper;

    public String draftApproval(
            DraftApproval dto, List<MultipartFile> files, Long employeeId, ApprovalStatus status) {
        // HR 모듈에서 기안자(사원) 정보 조회
        EmployeeProfileDTO drafterProfile = hrFacade.getEmployeeProfile(employeeId);

        ElectronicApprovalParam approval =
                ElectronicApprovalParam.from(dto, drafterProfile, status);

        approvalMapper.insertElectronicApproval(approval);
        insertDetailByDocType(dto, approval.getApprovalId());
        insertApprovalLines(dto.getApprovalLine(), approval.getApprovalId(), status);
        insertReferenceAndRecipientLines(
                dto.getReferenceLine(), dto.getReceipientLine(), approval.getApprovalId());
        insertAttachments(files, approval.getApprovalId());

        if (status.equals(ApprovalStatus.PENDING)) {
            return dto.getTitle() + " 기안이 상신되었습니다.";
        } else {
            return "기안이 임시 저장 되었습니다.";
        }
    }

    @Transactional(readOnly = true)
    public DownloadedApprovalFile downloadAttachment(Long approvalId, Long fileId) {
        ApprovalAttachmentRow attachment =
                approvalAttachmentMapper
                        .findAttachmentByFileIdAndApprovalId(fileId, approvalId)
                        .orElseThrow(
                                () ->
                                        new AttachmentNotFoundException(
                                                "첨부파일을 찾을 수 없습니다. approvalId="
                                                        + approvalId
                                                        + ", fileId="
                                                        + fileId));

        byte[] content = approvalFileService.downloadByFileUrl(attachment.filePath());
        return new DownloadedApprovalFile(attachment.originalName(), content);
    }

    public void deleteApproval(Long approvalId, Long employeeId) {
        if (approvalMapper.countByApprovalId(approvalId) == 0) {
            throw new ApprovalNotFoundException("존재하지 않는 기안입니다.");
        }
        if (approvalMapper.countByApprovalIdAndDrafterId(approvalId, employeeId) == 0) {
            throw new ForbiddenException("본인이 기안한 문서만 삭제할 수 있습니다.");
        }

        List<ApprovalAttachmentRow> attachments =
                approvalAttachmentMapper.findAttachmentsByApprovalId(approvalId);
        attachments.forEach(
                attachment -> approvalFileService.deleteByFileUrl(attachment.filePath()));

        int deleted = approvalMapper.deleteElectronicApprovalById(approvalId);
        if (deleted != 1) {
            throw new IllegalStateException("기안 삭제에 실패했습니다. approvalId=" + approvalId);
        }
    }

    private void insertDetailByDocType(DraftApproval dto, Long approvalId) {
        switch (dto.getDocType()) {
            case VACATION -> {
                if (dto.getVacationRequest() == null) {
                    throw new FormRequiredException("휴가 신청 시 휴가 양식이 필요합니다.");
                }
                vacationMapper.insertVacationDetail(
                        VacationDetailParam.from(dto.getVacationRequest(), approvalId));
            }
            case OVERTIME -> {
                if (dto.getOvertimeRequest() == null) {
                    throw new FormRequiredException("연장근무 신청 시 연장근무 양식이 필요합니다.");
                }
                overtimeMapper.insertOvertimeDetail(
                        OvertimeDetailParam.from(dto.getOvertimeRequest(), approvalId));
            }
            case FLEXIBLE -> {
                if (dto.getFlexibleWorkRequest() == null) {
                    throw new FormRequiredException("유연근무 신청 시 유연근무 양식이 필요합니다.");
                }
                flexibleWorkMapper.insertFlexibleWorkDetail(
                        FlexibleWorkParam.from(dto.getFlexibleWorkRequest(), approvalId));
            }
            case TRIP -> {
                if (dto.getBusinessTripRequest() == null) {
                    throw new FormRequiredException("외근 및 출장 신청 시 외근 및 출장 양식이 필요합니다.");
                }
                businessTripMapper.insertBusinessTripDetail(
                        BusinessTripDetailParam.from(dto.getBusinessTripRequest(), approvalId));
            }
            case LEAVE -> {
                if (dto.getLeaveRequest() == null) {
                    throw new FormRequiredException("휴직 신청 시 휴직 양식이 필요합니다.");
                }
                leaveMapper.insertLeaveDetail(
                        LeaveDetailParam.from(dto.getLeaveRequest(), approvalId));
            }
            case RTW -> {
                if (dto.getRtwRequest() == null) {
                    throw new FormRequiredException("복직 신청 시 복직 양식이 필요합니다.");
                }
                rtwMapper.insertRtwDetail(RTWDetailParam.from(dto.getRtwRequest(), approvalId));
            }
        }
    }

    private void insertApprovalLines(
            List<ApprovalLineRequest> approvalLineDto, Long approvalId, ApprovalStatus status) {
        if (approvalLineDto != null) {
            approvalLineDto.forEach(
                    line -> {
                        EmployeeProfileDTO profile =
                                hrFacade.getEmployeeProfile(line.getApproverId());
                        approvalLineMapper.insertApprovalLine(
                                ApprovalLineParam.from(
                                        line,
                                        approvalId,
                                        status,
                                        profile.employeeName(),
                                        profile.rankName()));
                    });
        }
    }

    private void insertReferenceAndRecipientLines(
            List<ReferenceLineRequest> ref_dto,
            List<RecipientLineRequest> rec_dto,
            Long approvalId) {
        if (ref_dto != null) {
            ref_dto.forEach(
                    line -> {
                        EmployeeProfileDTO profile =
                                hrFacade.getEmployeeProfile(line.getReferencerId());
                        referenceLineMapper.insertReferenceLine(
                                ReferenceLineParam.from(
                                        line.getReferencerId(),
                                        approvalId,
                                        profile.employeeName() == null
                                                ? "미지정"
                                                : profile.employeeName(),
                                        profile.rankName() == null ? "미지정" : profile.rankName()));
                    });
        }

        if (rec_dto != null) {
            rec_dto.forEach(
                    line -> {
                        EmployeeProfileDTO profile =
                                hrFacade.getEmployeeProfile(line.getReceipientId());
                        recipientLineMapper.insertRecipientLine(
                                RecipientLineParam.from(
                                        line.getReceipientId(),
                                        approvalId,
                                        profile.employeeName() == null
                                                ? "미지정"
                                                : profile.employeeName(),
                                        profile.rankName() == null ? "미지정" : profile.rankName()));
                    });
        }
    }

    private void insertAttachments(List<MultipartFile> files, Long approvalId) {
        if (files == null || files.isEmpty()) {
            return;
        }

        List<String> uploadedKeys = new ArrayList<>();
        String dir = "approval/" + approvalId;

        try {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                ApprovalFileService.UploadResult uploaded = approvalFileService.upload(file, dir);
                uploadedKeys.add(uploaded.key());

                approvalAttachmentMapper.insertApprovalAttachment(
                        ApprovalAttachmentParam.from(
                                uploaded.fileUrl(), uploaded.originalName(), approvalId));
            }
        } catch (RuntimeException e) {
            uploadedKeys.forEach(
                    key -> {
                        try {
                            approvalFileService.delete(key);
                        } catch (RuntimeException deleteEx) {
                            log.warn("첨부파일 보상 삭제 실패. key={}", key, deleteEx);
                        }
                    });
            throw e;
        }
    }
}
