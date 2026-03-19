package com.reverse.approval.internal.application;

import com.reverse.approval.ApprovalFacade;
import com.reverse.approval.internal.domain.enums.ApprovalStatus;
import com.reverse.approval.internal.domain.enums.DocumentBoxType;
import com.reverse.approval.internal.domain.enums.ProgressTabType;
import com.reverse.approval.internal.domain.enums.VacationType;
import com.reverse.approval.internal.dto.request.ApprovalLineRequest;
import com.reverse.approval.internal.dto.request.ApprovalProcessRequest;
import com.reverse.approval.internal.dto.request.DraftApproval;
import com.reverse.approval.internal.dto.request.RecipientLineRequest;
import com.reverse.approval.internal.dto.request.ReferenceLineRequest;
import com.reverse.approval.internal.dto.response.ApprovalBoxPageResponse;
import com.reverse.approval.internal.dto.response.ApprovalCreatedResponse;
import com.reverse.approval.internal.dto.response.ApprovalDashboardResponse;
import com.reverse.approval.internal.dto.response.ApprovalDetailResponse;
import com.reverse.approval.internal.dto.response.ApprovalFlexiblePageResponse;
import com.reverse.approval.internal.dto.response.ApprovalMainSummaryResponse;
import com.reverse.approval.internal.dto.response.ApprovalProgressOverviewResponse;
import com.reverse.approval.internal.dto.response.ApprovalProgressPageResponse;
import com.reverse.approval.internal.dto.response.ApprovalReviewPageResponse;
import com.reverse.approval.internal.dto.response.ApprovalVacationPageResponse;
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
import com.reverse.approval.internal.persistence.row.ApprovalBoxRow;
import com.reverse.approval.internal.persistence.row.ApprovalDashboardMyDraftRow;
import com.reverse.approval.internal.persistence.row.ApprovalDashboardPendingReviewRow;
import com.reverse.approval.internal.persistence.row.ApprovalFlexibleRow;
import com.reverse.approval.internal.persistence.row.ApprovalHeaderRow;
import com.reverse.approval.internal.persistence.row.ApprovalLineDetailRow;
import com.reverse.approval.internal.persistence.row.ApprovalLineRow;
import com.reverse.approval.internal.persistence.row.ApprovalMainItemRow;
import com.reverse.approval.internal.persistence.row.ApprovalProgressCountsRow;
import com.reverse.approval.internal.persistence.row.ApprovalProgressRow;
import com.reverse.approval.internal.persistence.row.ApprovalReviewRow;
import com.reverse.approval.internal.persistence.row.ApprovalVacationRow;
import com.reverse.approval.internal.persistence.row.BusinessTripDetailRow;
import com.reverse.approval.internal.persistence.row.FlexibleWorkDetailRow;
import com.reverse.approval.internal.persistence.row.LeaveDetailRow;
import com.reverse.approval.internal.persistence.row.OvertimeDetailRow;
import com.reverse.approval.internal.persistence.row.RTWDetailRow;
import com.reverse.approval.internal.persistence.row.RecipientLineDetailRow;
import com.reverse.approval.internal.persistence.row.ReferenceLineDetailRow;
import com.reverse.approval.internal.persistence.row.VacationDetailRow;
import com.reverse.core.event.ApprovalFlexibleEvent;
import com.reverse.core.event.ApprovalLeaveEvent;
import com.reverse.core.event.ApprovalOvertimeEvent;
import com.reverse.core.event.ApprovalRTWEvent;
import com.reverse.core.event.ApprovalTripEvent;
import com.reverse.core.event.ApprovalVacationEvent;
import com.reverse.core.event.EmailSendEvent;
import com.reverse.core.exception.BadRequestException;
import com.reverse.core.exception.ForbiddenException;
import com.reverse.core.service.NumberingService;
import com.reverse.core.service.S3StorageService;
import com.reverse.hr.HrFacade;
import com.reverse.hr.dto.EmployeeProfileDTO;
import com.reverse.hr.dto.OrganizationMemberInfo;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
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
    private final S3StorageService s3StorageService;
    private final ApprovalAttachmentMapper approvalAttachmentMapper;
    private final NumberingService numberingService;
    private final ApplicationEventPublisher eventPublisher;

    public ApprovalCreatedResponse draftApproval(
            DraftApproval dto, List<MultipartFile> files, Long employeeId, ApprovalStatus status) {
        if (ApprovalStatus.PENDING.equals(status)
                && (dto.getApprovalLine() == null || dto.getApprovalLine().isEmpty())) {
            throw new BadRequestException("결재선은 최소 1명 이상 지정해야 합니다.");
        }

        List<String> uploadedKeys = new ArrayList<>();
        registerRollbackCleanup(uploadedKeys);

        // HR 모듈에서 기안자(사원) 정보 조회
        EmployeeProfileDTO drafterProfile = hrFacade.getEmployeeProfile(employeeId);

        ElectronicApprovalParam approval =
                ElectronicApprovalParam.from(dto, drafterProfile, status);

        approvalMapper.insertElectronicApproval(approval);
        insertDetailByDocType(dto, approval.getApprovalId());
        insertApprovalLines(dto.getApprovalLine(), approval.getApprovalId(), status);
        insertReferenceAndRecipientLines(
                dto.getReferenceLine(), dto.getReceipientLine(), approval.getApprovalId());
        insertAttachments(files, approval.getApprovalId(), uploadedKeys);
        if (ApprovalStatus.PENDING.equals(status)) {
            String docId = numberingService.generateSequence("DOC");
            approvalMapper.updateDocId(approval.getApprovalId(), docId);
        }

        if (status.equals(ApprovalStatus.PENDING)) {
            publishSubmissionMailEvents(dto, drafterProfile);

            return new ApprovalCreatedResponse(
                    approval.getApprovalId(), dto.getTitle() + " 기안이 상신되었습니다.");
        } else {
            return new ApprovalCreatedResponse(approval.getApprovalId(), "기안이 임시 저장 되었습니다.");
        }
    }

    @Transactional(readOnly = true)
    public DownloadedApprovalFile downloadAttachment(
            Long approvalId, Long fileId, Long employeeId) {
        validateReadableApproval(approvalId, employeeId);

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

        byte[] content = s3StorageService.downloadByKey(attachment.fileKey());
        return new DownloadedApprovalFile(attachment.originalName(), content);
    }

    @Transactional(readOnly = true)
    public ApprovalDetailResponse getApprovalDetail(Long approvalId, Long employeeId) {
        ApprovalHeaderRow header = validateReadableApproval(approvalId, employeeId);

        List<ApprovalDetailResponse.ApprovalLineItem> approvalLines =
                approvalLineMapper.findLinesByApprovalId(approvalId).stream()
                        .map(this::toApprovalLineItem)
                        .toList();

        List<ApprovalDetailResponse.ReferenceLineItem> referenceLines =
                referenceLineMapper.findReferenceLinesByApprovalId(approvalId).stream()
                        .map(this::toReferenceLineItem)
                        .toList();

        List<ApprovalDetailResponse.RecipientLineItem> recipientLines =
                recipientLineMapper.findRecipientLinesByApprovalId(approvalId).stream()
                        .map(this::toRecipientLineItem)
                        .toList();

        List<ApprovalDetailResponse.AttachmentItem> attachments =
                approvalAttachmentMapper.findAttachmentsByApprovalId(approvalId).stream()
                        .map(this::toAttachmentItem)
                        .toList();

        ApprovalDetailResponse.VacationDetail vacationDetail = null;
        ApprovalDetailResponse.OvertimeDetail overtimeDetail = null;
        ApprovalDetailResponse.FlexibleWorkDetail flexibleWorkDetail = null;
        ApprovalDetailResponse.BusinessTripDetail businessTripDetail = null;
        ApprovalDetailResponse.LeaveDetail leaveDetail = null;
        ApprovalDetailResponse.RTWDetail rtwDetail = null;

        switch (header.docType()) {
            case "VACATION" -> {
                VacationDetailRow row =
                        vacationMapper
                                .findVacationDetailByApprovalId(approvalId)
                                .orElseThrow(
                                        () -> new ApprovalNotFoundException("기안 상세를 찾을 수 없습니다."));
                vacationDetail =
                        new ApprovalDetailResponse.VacationDetail(
                                row.vacationType(), row.startDate(), row.endDate(), row.reason());
            }
            case "OVERTIME" -> {
                OvertimeDetailRow row =
                        overtimeMapper
                                .findOvertimeDetailByApprovalId(approvalId)
                                .orElseThrow(
                                        () -> new ApprovalNotFoundException("기안 상세를 찾을 수 없습니다."));
                overtimeDetail =
                        new ApprovalDetailResponse.OvertimeDetail(
                                row.workDate(), row.startTime(), row.endTime(), row.reason());
            }
            case "FLEXIBLE" -> {
                FlexibleWorkDetailRow row =
                        flexibleWorkMapper
                                .findFlexibleWorkDetailByApprovalId(approvalId)
                                .orElseThrow(
                                        () -> new ApprovalNotFoundException("기안 상세를 찾을 수 없습니다."));
                flexibleWorkDetail =
                        new ApprovalDetailResponse.FlexibleWorkDetail(
                                row.startDate(), row.endDate(), row.reason());
            }
            case "TRIP" -> {
                BusinessTripDetailRow row =
                        businessTripMapper
                                .findBusinessTripDetailByApprovalId(approvalId)
                                .orElseThrow(
                                        () -> new ApprovalNotFoundException("기안 상세를 찾을 수 없습니다."));
                businessTripDetail =
                        new ApprovalDetailResponse.BusinessTripDetail(
                                row.tripType(),
                                row.destination(),
                                row.startDate(),
                                row.endDate(),
                                row.reason());
            }
            case "LEAVE" -> {
                LeaveDetailRow row =
                        leaveMapper
                                .findLeaveDetailByApprovalId(approvalId)
                                .orElseThrow(
                                        () -> new ApprovalNotFoundException("기안 상세를 찾을 수 없습니다."));
                leaveDetail =
                        new ApprovalDetailResponse.LeaveDetail(
                                row.startDate(), row.endDate(), row.leaveType(), row.reason());
            }
            case "RTW" -> {
                RTWDetailRow row =
                        rtwMapper
                                .findRTWDetailByApprovalId(approvalId)
                                .orElseThrow(
                                        () -> new ApprovalNotFoundException("기안 상세를 찾을 수 없습니다."));
                rtwDetail = new ApprovalDetailResponse.RTWDetail(row.rtwDate(), row.reason());
            }
            default -> throw new ApprovalNotFoundException("지원하지 않는 문서 타입입니다.");
        }

        return new ApprovalDetailResponse(
                header.approvalId(),
                header.docId(),
                header.docType(),
                header.title(),
                header.approvalStatus(),
                header.draftDate(),
                header.approveDate(),
                header.drafterId(),
                header.drafterName(),
                header.departmentName(),
                approvalLines,
                referenceLines,
                recipientLines,
                attachments,
                vacationDetail,
                overtimeDetail,
                flexibleWorkDetail,
                businessTripDetail,
                leaveDetail,
                rtwDetail);
    }

    public void markApprovalAsRead(Long approvalId, Long employeeId) {
        validateReadableApproval(approvalId, employeeId);
        updateReadDateIfNull(approvalId, employeeId);
    }

    @Transactional(readOnly = true)
    public ApprovalBoxPageResponse getApprovalBoxes(
            Long employeeId, DocumentBoxType boxType, int page, int size) {
        if (page < 0) {
            throw new BadRequestException("page는 0 이상이어야 합니다.");
        }
        if (size <= 0) {
            throw new BadRequestException("size는 1 이상이어야 합니다.");
        }

        int totalElements = approvalMapper.countApprovalsByBox(employeeId, boxType.name());
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        int offset = page * size;

        List<ApprovalBoxPageResponse.ApprovalBoxItem> content =
                approvalMapper.findApprovalsByBox(employeeId, boxType.name(), offset, size).stream()
                        .map(this::toApprovalBoxItem)
                        .toList();

        boolean hasNext = page + 1 < totalPages;
        return new ApprovalBoxPageResponse(content, page, size, totalElements, totalPages, hasNext);
    }

    @Transactional(readOnly = true)
    public ApprovalProgressOverviewResponse getApprovalProgressOverview(
            Long employeeId, int page, int size) {
        ApprovalProgressCountsRow countsRow = approvalMapper.findApprovalProgressCounts(employeeId);
        ApprovalProgressPageResponse pageResponse =
                getApprovalProgressPage(employeeId, ProgressTabType.ALL, null, page, size);

        return new ApprovalProgressOverviewResponse(
                new ApprovalProgressOverviewResponse.Counts(
                        nvl(countsRow.allCount()),
                        nvl(countsRow.draftCount()),
                        nvl(countsRow.inProgressCount()),
                        nvl(countsRow.rejectedCount())),
                pageResponse);
    }

    @Transactional(readOnly = true)
    public ApprovalProgressPageResponse searchApprovalProgress(
            Long employeeId, ProgressTabType tabType, String keyword, int page, int size) {
        return getApprovalProgressPage(employeeId, tabType, keyword, page, size);
    }

    @Transactional(readOnly = true)
    public ApprovalReviewPageResponse getApprovalReviews(Long employeeId, int page, int size) {
        if (page < 0) {
            throw new BadRequestException("page는 0 이상이어야 합니다.");
        }
        if (size <= 0) {
            throw new BadRequestException("size는 1 이상이어야 합니다.");
        }

        int totalElements = nvl(approvalMapper.countApprovalReviews(employeeId));
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        int offset = page * size;

        List<ApprovalReviewPageResponse.ApprovalReviewItem> content =
                approvalMapper.findApprovalReviews(employeeId, offset, size).stream()
                        .map(this::toApprovalReviewItem)
                        .toList();

        boolean hasNext = page + 1 < totalPages;
        return new ApprovalReviewPageResponse(
                content, page, size, totalElements, totalPages, hasNext);
    }

    @Transactional(readOnly = true)
    public ApprovalVacationPageResponse getAdminVacationList(Long employeeId, int page, int size) {
        if (page < 0) {
            throw new BadRequestException("page는 0 이상이어야 합니다.");
        }
        if (size <= 0) {
            throw new BadRequestException("size는 1 이상이어야 합니다.");
        }

        List<Long> employeeIds =
                hrFacade.getMyOrganizationMembers(employeeId).stream()
                        .map(OrganizationMemberInfo::employeeId)
                        .distinct()
                        .toList();

        if (employeeIds.isEmpty()) {
            return new ApprovalVacationPageResponse(List.of(), page, size, 0, 0, false);
        }

        int totalElements = nvl(approvalMapper.countAdminVacationApprovals(employeeIds));
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        long offsetLong = (long) page * size;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new BadRequestException("조회 가능한 페이지 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;

        List<ApprovalVacationPageResponse.ApprovalVacationItem> content =
                approvalMapper.findAdminVacationApprovals(employeeIds, offset, size).stream()
                        .map(this::toApprovalVacationItem)
                        .toList();

        boolean hasNext = page + 1 < totalPages;
        return new ApprovalVacationPageResponse(
                content, page, size, totalElements, totalPages, hasNext);
    }

    @Transactional(readOnly = true)
    public ApprovalFlexiblePageResponse getAdminFlexibleList(Long employeeId, int page, int size) {
        if (page < 0) {
            throw new BadRequestException("page는 0 이상이어야 합니다.");
        }
        if (size <= 0) {
            throw new BadRequestException("size는 1 이상이어야 합니다.");
        }

        List<Long> employeeIds =
                hrFacade.getMyOrganizationMembers(employeeId).stream()
                        .map(OrganizationMemberInfo::employeeId)
                        .distinct()
                        .toList();

        if (employeeIds.isEmpty()) {
            return new ApprovalFlexiblePageResponse(List.of(), page, size, 0, 0, false);
        }

        int totalElements = nvl(approvalMapper.countAdminFlexibleApprovals(employeeIds));
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        long offsetLong = (long) page * size;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new BadRequestException("조회 가능한 페이지 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;

        List<ApprovalFlexiblePageResponse.ApprovalFlexibleItem> content =
                approvalMapper.findAdminFlexibleApprovals(employeeIds, offset, size).stream()
                        .map(this::toApprovalFlexibleItem)
                        .toList();

        boolean hasNext = page + 1 < totalPages;
        return new ApprovalFlexiblePageResponse(
                content, page, size, totalElements, totalPages, hasNext);
    }

    @Transactional(readOnly = true)
    public ApprovalDashboardResponse getApprovalDashboard(Long employeeId) {
        ApprovalReviewPageResponse reviewPage = getApprovalReviews(employeeId, 0, 5);
        ApprovalProgressOverviewResponse progressOverview =
                getApprovalProgressOverview(employeeId, 0, 5);
        List<ApprovalDashboardResponse.PendingReviewItem> pendingReviewDocuments =
                approvalMapper.findApprovalDashboardPendingReviews(employeeId, 5).stream()
                        .map(this::toDashboardPendingReviewItem)
                        .toList();

        List<ApprovalDashboardResponse.MyDraftItem> myDrafts =
                progressOverview.page().content().stream()
                        .map(this::toDashboardMyDraftItemFromProgress)
                        .toList();

        return new ApprovalDashboardResponse(
                new ApprovalDashboardResponse.Counts(
                        Math.toIntExact(reviewPage.totalElements()),
                        progressOverview.counts().inProgressCount(),
                        nvl(approvalMapper.countDashboardCompletedThisMonth(employeeId))),
                pendingReviewDocuments,
                myDrafts);
    }

    @Transactional(readOnly = true)
    public ApprovalMainSummaryResponse getApprovalMainSummary(Long employeeId) {
        int pendingCount = nvl(approvalMapper.countMainPendingReviews(employeeId));
        int inProgressCount = nvl(approvalMapper.countMainInProgressApprovals(employeeId));

        List<ApprovalMainItemRow> pendingRows =
                approvalMapper.findMainPendingReviews(employeeId, 5);
        List<ApprovalMainItemRow> inProgressRows =
                approvalMapper.findMainInProgressApprovals(employeeId, 5);

        List<ApprovalMainSummaryResponse.MainItem> pendingDocuments =
                pendingRows.stream()
                        .map(
                                row ->
                                        new ApprovalMainSummaryResponse.MainItem(
                                                row.approvalId(),
                                                row.title(),
                                                row.drafterName(),
                                                row.draftDate(),
                                                row.readDate()))
                        .toList();

        List<ApprovalMainSummaryResponse.MainItem> inProgressDocuments =
                inProgressRows.stream()
                        .map(
                                row ->
                                        new ApprovalMainSummaryResponse.MainItem(
                                                row.approvalId(),
                                                row.title(),
                                                "-",
                                                row.draftDate(),
                                                row.readDate()))
                        .toList();

        return new ApprovalMainSummaryResponse(
                pendingCount, inProgressCount, pendingDocuments, inProgressDocuments);
    }

    public void deleteApproval(Long approvalId, Long employeeId) {
        if (approvalMapper.countByApprovalId(approvalId) == 0) {
            throw new ApprovalNotFoundException("존재하지 않는 기안입니다.");
        }
        if (approvalMapper.countByApprovalIdAndDrafterId(approvalId, employeeId) == 0) {
            throw new ForbiddenException("본인이 기안한 문서만 삭제할 수 있습니다.");
        }

        String approvalStatus = approvalMapper.findApprovalStatusByApprovalId(approvalId);
        if (!ApprovalStatus.TEMP.name().equals(approvalStatus)) {
            throw new BadRequestException("임시 저장 상태(TEMP) 문서만 삭제할 수 있습니다.");
        }

        List<ApprovalAttachmentRow> attachments =
                approvalAttachmentMapper.findAttachmentsByApprovalId(approvalId);

        int deleted = approvalMapper.deleteElectronicApprovalById(approvalId);
        if (deleted != 1) {
            throw new IllegalStateException("기안 삭제에 실패했습니다. approvalId=" + approvalId);
        }

        attachments.forEach(
                attachment -> {
                    if (!StringUtils.hasText(attachment.fileKey())) {
                        return;
                    }
                    try {
                        s3StorageService.deleteByKey(attachment.fileKey());
                    } catch (RuntimeException e) {
                        log.warn("첨부파일 후처리 삭제 실패. key={}", attachment.fileKey(), e);
                    }
                });
    }

    public ApprovalCreatedResponse reDraftApproval(
            Long approvalId, DraftApproval dto, List<MultipartFile> files, Long employeeId) {
        if (dto.getApprovalLine() == null || dto.getApprovalLine().isEmpty()) {
            throw new BadRequestException("결재선은 최소 1명 이상 지정해야 합니다.");
        }

        if (approvalMapper.countByApprovalId(approvalId) == 0) {
            throw new ApprovalNotFoundException("존재하지 않는 기안입니다.");
        }
        if (approvalMapper.countByApprovalIdAndDrafterId(approvalId, employeeId) == 0) {
            throw new ForbiddenException("본인이 기안한 문서만 재상신할 수 있습니다.");
        }

        List<String> oldAttachmentKeys =
                approvalAttachmentMapper.findAttachmentsByApprovalId(approvalId).stream()
                        .map(ApprovalAttachmentRow::fileKey)
                        .filter(StringUtils::hasText)
                        .toList();
        registerAfterCommitCleanup(oldAttachmentKeys);

        int deleted = approvalMapper.deleteElectronicApprovalById(approvalId);
        if (deleted != 1) {
            throw new IllegalStateException("재상신을 위한 기존 기안 삭제에 실패했습니다. approvalId=" + approvalId);
        }

        return draftApproval(dto, files, employeeId, ApprovalStatus.PENDING);
    }

    public void processApproval(Long approvalId, ApprovalProcessRequest request, Long approverId) {
        if (approvalMapper.countByApprovalId(approvalId) == 0) {
            throw new ApprovalNotFoundException("존재하지 않는 기안입니다.");
        }

        String approvalStatus = approvalMapper.findApprovalStatusByApprovalId(approvalId);
        if (!ApprovalStatus.PENDING.name().equals(approvalStatus)) {
            throw new BadRequestException("결재 진행 중(PENDING) 문서만 처리할 수 있습니다.");
        }

        ApprovalLineRow currentLine =
                approvalLineMapper.findFirstPendingLineByApprovalId(approvalId);
        if (currentLine == null) {
            throw new BadRequestException("처리 가능한 결재선이 없습니다.");
        }
        if (!currentLine.approverId().equals(approverId)) {
            throw new ForbiddenException("현재 결재 순서의 결재자만 처리할 수 있습니다.");
        }

        String normalizedReason = request.reason();
        if (normalizedReason != null) {
            normalizedReason = normalizedReason.trim();
            if (normalizedReason.isBlank()) {
                normalizedReason = null;
            }
        }

        if (Boolean.TRUE.equals(request.approve())) {
            processApprove(approvalId, currentLine, normalizedReason, approverId);
            return;
        }

        if (Boolean.FALSE.equals(request.approve())) {
            if (normalizedReason == null) {
                throw new BadRequestException("반려 시 사유는 필수입니다.");
            }
            processReject(approvalId, currentLine, normalizedReason, approverId);
            return;
        }

        throw new BadRequestException("유효하지 않은 결재 처리 요청입니다.");
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
                                        approvalId,
                                        line.getReferencerId(),
                                        profile.employeeName() == null
                                                ? "미지정"
                                                : profile.employeeName(),
                                        profile.rankName() == null ? "미지정" : profile.rankName()));
                    });
        }

        if (rec_dto != null) {
            Set<Long> uniqueRecipientIds = new LinkedHashSet<>();
            rec_dto.forEach(line -> uniqueRecipientIds.add(line.getReceipientId()));

            uniqueRecipientIds.forEach(
                    receiverId -> {
                        EmployeeProfileDTO profile = hrFacade.getEmployeeProfile(receiverId);
                        recipientLineMapper.insertRecipientLine(
                                RecipientLineParam.from(
                                        approvalId,
                                        receiverId,
                                        profile.employeeName() == null
                                                ? "미지정"
                                                : profile.employeeName(),
                                        profile.rankName() == null ? "미지정" : profile.rankName()));
                    });
        }
    }

    private void insertAttachments(
            List<MultipartFile> files, Long approvalId, List<String> uploadedKeys) {
        if (files == null || files.isEmpty()) {
            return;
        }

        String dir = "approval/" + approvalId;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            S3StorageService.UploadResult uploaded = s3StorageService.upload(file, dir);
            uploadedKeys.add(uploaded.key());

            approvalAttachmentMapper.insertApprovalAttachment(
                    ApprovalAttachmentParam.from(
                            uploaded.key(),
                            uploaded.fileUrl(),
                            uploaded.originalName(),
                            approvalId));
        }
    }

    private void registerRollbackCleanup(List<String> uploadedKeys) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != TransactionSynchronization.STATUS_ROLLED_BACK
                                || uploadedKeys.isEmpty()) {
                            return;
                        }

                        uploadedKeys.forEach(
                                key -> {
                                    try {
                                        s3StorageService.delete(key);
                                    } catch (RuntimeException e) {
                                        log.warn("롤백 보상 삭제 실패. key={}", key, e);
                                    }
                                });
                    }
                });
    }

    private void registerAfterCommitCleanup(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            fileKeys.forEach(
                    key -> {
                        try {
                            s3StorageService.deleteByKey(key);
                        } catch (RuntimeException e) {
                            log.warn("커밋 후 첨부파일 삭제 실패. key={}", key, e);
                        }
                    });
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        fileKeys.forEach(
                                key -> {
                                    try {
                                        s3StorageService.deleteByKey(key);
                                    } catch (RuntimeException e) {
                                        log.warn("커밋 후 첨부파일 삭제 실패. key={}", key, e);
                                    }
                                });
                    }
                });
    }

    private void publishSubmissionMailEvents(DraftApproval dto, EmployeeProfileDTO drafterProfile) {
        String title = dto.getTitle();
        String drafterName =
                drafterProfile.employeeName() == null ? "기안자" : drafterProfile.employeeName();

        sendMailToFirstApprover(dto, title, drafterName);
        sendMailToReferencers(dto, title, drafterName);
    }

    private void sendMailToFirstApprover(DraftApproval dto, String title, String drafterName) {
        if (dto.getApprovalLine() == null || dto.getApprovalLine().isEmpty()) {
            return;
        }

        Long firstApproverId =
                dto.getApprovalLine().stream()
                        .min(Comparator.comparingInt(ApprovalLineRequest::getApprovalSeq))
                        .map(ApprovalLineRequest::getApproverId)
                        .orElse(null);

        if (firstApproverId == null) {
            return;
        }

        EmployeeProfileDTO approver = hrFacade.getEmployeeProfile(firstApproverId);
        if (approver.email() == null || approver.email().isBlank()) {
            return;
        }

        String subject = "[RHIGHT] 결재 요청: " + title;
        String body = "<p>" + drafterName + "님이 결재 문서를 상신했습니다.</p><p>문서 제목: " + title + "</p>";
        safePublishEmailEvent(approver.email(), subject, body);
    }

    private void sendMailToReferencers(DraftApproval dto, String title, String drafterName) {
        if (dto.getReferenceLine() == null || dto.getReferenceLine().isEmpty()) {
            return;
        }

        Set<Long> referencerIds = new LinkedHashSet<>();
        dto.getReferenceLine()
                .forEach(
                        line -> {
                            if (line.getReferencerId() != null) {
                                referencerIds.add(line.getReferencerId());
                            }
                        });

        String subject = "[RHIGHT] 참조 문서 도착: " + title;
        String body = "<p>" + drafterName + "님이 참조 문서를 상신했습니다.</p><p>문서 제목: " + title + "</p>";

        referencerIds.forEach(
                referencerId -> {
                    EmployeeProfileDTO profile = hrFacade.getEmployeeProfile(referencerId);
                    if (profile.email() == null || profile.email().isBlank()) {
                        return;
                    }
                    safePublishEmailEvent(profile.email(), subject, body);
                });
    }

    private void safePublishEmailEvent(String to, String subject, String body) {
        try {
            eventPublisher.publishEvent(new EmailSendEvent(to, subject, body));
        } catch (RuntimeException e) {
            log.warn("이메일 이벤트 발행 실패. to={}, subject={}", to, subject, e);
        }
    }

    private ApprovalDetailResponse.ApprovalLineItem toApprovalLineItem(ApprovalLineDetailRow row) {
        return new ApprovalDetailResponse.ApprovalLineItem(
                row.approvalSeq(),
                row.approvalStatus(),
                row.approverId(),
                row.approverName(),
                row.approverRank(),
                row.reason(),
                row.approvedDate(),
                row.readDate());
    }

    private ApprovalDetailResponse.ReferenceLineItem toReferenceLineItem(
            ReferenceLineDetailRow row) {
        return new ApprovalDetailResponse.ReferenceLineItem(
                row.referencerId(), row.referencerName(), row.referenceRank(), row.readDate());
    }

    private ApprovalDetailResponse.RecipientLineItem toRecipientLineItem(
            RecipientLineDetailRow row) {
        return new ApprovalDetailResponse.RecipientLineItem(
                row.receiverId(), row.receiverName(), row.receiverRank(), row.readDate());
    }

    private ApprovalDetailResponse.AttachmentItem toAttachmentItem(ApprovalAttachmentRow row) {
        return new ApprovalDetailResponse.AttachmentItem(
                row.fileId(), row.filePath(), row.originalName(), row.createdDate());
    }

    private ApprovalBoxPageResponse.ApprovalBoxItem toApprovalBoxItem(ApprovalBoxRow row) {
        return new ApprovalBoxPageResponse.ApprovalBoxItem(
                row.approvalId(),
                row.docId(),
                row.docType(),
                row.title(),
                row.approvalStatus(),
                row.draftDate(),
                row.approveDate(),
                row.drafterId(),
                row.drafterName(),
                row.departmentName(),
                row.readDate());
    }

    private ApprovalProgressPageResponse getApprovalProgressPage(
            Long employeeId, ProgressTabType tabType, String keyword, int page, int size) {
        if (page < 0) {
            throw new BadRequestException("page는 0 이상이어야 합니다.");
        }
        if (size <= 0) {
            throw new BadRequestException("size는 1 이상이어야 합니다.");
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        int totalElements =
                approvalMapper.countApprovalProgress(employeeId, tabType.name(), normalizedKeyword);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        int offset = page * size;

        List<ApprovalProgressPageResponse.ApprovalProgressItem> content =
                approvalMapper
                        .findApprovalProgress(
                                employeeId, tabType.name(), normalizedKeyword, offset, size)
                        .stream()
                        .map(this::toApprovalProgressItem)
                        .toList();

        boolean hasNext = page + 1 < totalPages;
        return new ApprovalProgressPageResponse(
                content, page, size, totalElements, totalPages, hasNext);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ApprovalProgressPageResponse.ApprovalProgressItem toApprovalProgressItem(
            ApprovalProgressRow row) {
        return new ApprovalProgressPageResponse.ApprovalProgressItem(
                row.approvalId(),
                row.docId(),
                row.docType(),
                row.title(),
                row.approvalStatus(),
                row.draftDate(),
                row.readDate(),
                row.currentApproverName(),
                nvl(row.totalApproverCount()),
                nvl(row.doneApproverCount()),
                nvl(row.progressPercent()));
    }

    private int nvl(Number value) {
        return value == null ? 0 : value.intValue();
    }

    private ApprovalReviewPageResponse.ApprovalReviewItem toApprovalReviewItem(
            ApprovalReviewRow row) {
        return new ApprovalReviewPageResponse.ApprovalReviewItem(
                row.approvalId(),
                row.docId(),
                row.docType(),
                row.title(),
                row.approvalStatus(),
                row.drafterName(),
                row.departmentName(),
                row.draftDate());
    }

    private ApprovalVacationPageResponse.ApprovalVacationItem toApprovalVacationItem(
            ApprovalVacationRow row) {
        return new ApprovalVacationPageResponse.ApprovalVacationItem(
                row.approvalId(),
                row.docId(),
                row.docType(),
                row.approvalStatus(),
                row.drafterId(),
                row.drafterName(),
                row.departmentName(),
                row.vacationType(),
                row.startDate(),
                row.endDate(),
                calculateVacationDays(row.vacationType(), row.startDate(), row.endDate()),
                row.reason(),
                row.draftDate());
    }

    private ApprovalFlexiblePageResponse.ApprovalFlexibleItem toApprovalFlexibleItem(
            ApprovalFlexibleRow row) {
        return new ApprovalFlexiblePageResponse.ApprovalFlexibleItem(
                row.approvalId(),
                row.docId(),
                row.docType(),
                row.approvalStatus(),
                row.drafterId(),
                row.drafterName(),
                row.departmentName(),
                row.startDate(),
                row.endDate(),
                row.reason(),
                row.draftDate());
    }

    private double calculateVacationDays(
            String vacationType, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        if (VacationType.HALF.name().equalsIgnoreCase(vacationType)) {
            return 0.5;
        }
        long days = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate()) + 1;
        return Math.max(0, days);
    }

    private ApprovalDashboardResponse.PendingReviewItem toDashboardPendingReviewItem(
            ApprovalDashboardPendingReviewRow row) {
        return new ApprovalDashboardResponse.PendingReviewItem(
                row.approvalId(),
                row.docType(),
                row.title(),
                row.drafterName(),
                row.draftDate(),
                row.readDate());
    }

    private ApprovalDashboardResponse.MyDraftItem toDashboardMyDraftItem(
            ApprovalDashboardMyDraftRow row) {
        return new ApprovalDashboardResponse.MyDraftItem(
                row.approvalId(),
                row.docType(),
                row.title(),
                row.currentApproverName(),
                row.approvalStatus());
    }

    private ApprovalDashboardResponse.MyDraftItem toDashboardMyDraftItemFromProgress(
            ApprovalProgressPageResponse.ApprovalProgressItem row) {
        return new ApprovalDashboardResponse.MyDraftItem(
                row.approvalId(),
                row.docType(),
                row.title(),
                row.currentApproverName(),
                row.approvalStatus());
    }

    private void updateReadDateIfNull(Long approvalId, Long employeeId) {
        approvalMapper.updateReadDateIfNull(approvalId, employeeId);
        approvalLineMapper.updateReadDateIfNull(approvalId, employeeId);
        referenceLineMapper.updateReadDateIfNull(approvalId, employeeId);
        recipientLineMapper.updateReadDateIfNull(approvalId, employeeId);
    }

    private ApprovalHeaderRow validateReadableApproval(Long approvalId, Long employeeId) {
        ApprovalHeaderRow header =
                approvalMapper
                        .findApprovalHeaderByApprovalId(approvalId)
                        .orElseThrow(() -> new ApprovalNotFoundException("존재하지 않는 기안입니다."));

        if (ApprovalStatus.TEMP.name().equals(header.approvalStatus())
                && !header.drafterId().equals(employeeId)) {
            throw new ForbiddenException("임시 저장 문서는 기안자만 조회할 수 있습니다.");
        }

        boolean canAccess =
                header.drafterId().equals(employeeId)
                        || approvalLineMapper.countByApprovalIdAndApproverId(approvalId, employeeId)
                                > 0
                        || referenceLineMapper.countByApprovalIdAndReferencerId(
                                        approvalId, employeeId)
                                > 0
                        || recipientLineMapper.countByApprovalIdAndReceiverId(
                                        approvalId, employeeId)
                                > 0;

        if (!canAccess) {
            throw new ForbiddenException("해당 기안을 조회할 권한이 없습니다.");
        }
        return header;
    }

    private void processApprove(
            Long approvalId, ApprovalLineRow currentLine, String reason, Long approverId) {
        int updatedLineCount =
                approvalLineMapper.updateApprovalLineToComplete(
                        currentLine.approvalLineId(), reason);
        if (updatedLineCount != 1) {
            throw new BadRequestException("이미 처리된 결재선입니다.");
        }

        int pendingCount = approvalLineMapper.countPendingLinesByApprovalId(approvalId);
        Long drafterId = approvalMapper.findDrafterIdByApprovalId(approvalId);
        String title = approvalMapper.findTitleByApprovalId(approvalId);
        EmployeeProfileDTO approverProfile = hrFacade.getEmployeeProfile(approverId);
        String approverName =
                approverProfile.employeeName() == null ? "결재자" : approverProfile.employeeName();
        String safeTitle = (title == null || title.isBlank()) ? "제목 없음" : title;

        Set<Long> recipients = new LinkedHashSet<>();
        recipients.add(drafterId);
        recipients.addAll(referenceLineMapper.findReferencerIdsByApprovalId(approvalId));

        if (pendingCount == 0) {
            int updatedApprovalCount = approvalMapper.updateApprovalToComplete(approvalId);
            if (updatedApprovalCount != 1) {
                throw new BadRequestException("이미 처리된 문서입니다.");
            }
            publishFinalApprovedEvent(approvalId);
            recipients.addAll(recipientLineMapper.findRecipientIdsByApprovalId(approvalId));
            publishMailToEmployeeIds(
                    recipients,
                    "[RHIGHT] 결재 완료: " + safeTitle,
                    "<p>" + approverName + "님이 결재를 완료했습니다.</p><p>문서 제목: " + safeTitle + "</p>");
            return;
        }

        ApprovalLineRow nextPendingLine =
                approvalLineMapper.findFirstPendingLineByApprovalId(approvalId);
        if (nextPendingLine != null) {
            recipients.add(nextPendingLine.approverId());
        }
        publishMailToEmployeeIds(
                recipients,
                "[RHIGHT] 결재 진행 알림: " + safeTitle,
                "<p>" + approverName + "님이 결재를 완료했습니다.</p><p>문서 제목: " + safeTitle + "</p>");
    }

    private void processReject(
            Long approvalId, ApprovalLineRow currentLine, String reason, Long approverId) {
        int updatedLineCount =
                approvalLineMapper.updateApprovalLineToRejected(
                        currentLine.approvalLineId(), reason);
        if (updatedLineCount != 1) {
            throw new BadRequestException("이미 처리된 결재선입니다.");
        }
        int updatedApprovalCount = approvalMapper.updateApprovalToRejected(approvalId);
        if (updatedApprovalCount != 1) {
            throw new BadRequestException("이미 처리된 문서입니다.");
        }

        Long drafterId = approvalMapper.findDrafterIdByApprovalId(approvalId);
        if (drafterId == null) {
            return;
        }

        EmployeeProfileDTO approverProfile = hrFacade.getEmployeeProfile(approverId);
        EmployeeProfileDTO drafterProfile = hrFacade.getEmployeeProfile(drafterId);
        if (drafterProfile.email() == null || drafterProfile.email().isBlank()) {
            return;
        }

        String title = approvalMapper.findTitleByApprovalId(approvalId);
        String safeTitle = (title == null || title.isBlank()) ? "제목 없음" : title;
        String approverName =
                approverProfile.employeeName() == null ? "결재자" : approverProfile.employeeName();

        safePublishEmailEvent(
                drafterProfile.email(),
                "[RHIGHT] 결재 반려: " + safeTitle,
                "<p>" + approverName + "님이 문서를 반려했습니다.</p><p>문서 제목: " + safeTitle + "</p>");
    }

    private void processHold(
            Long approvalId, ApprovalLineRow currentLine, String reason, Long approverId) {
        int updatedLineCount =
                approvalLineMapper.updateApprovalLineToHold(currentLine.approvalLineId(), reason);
        if (updatedLineCount != 1) {
            throw new BadRequestException("이미 처리된 결재선입니다.");
        }
        int updatedApprovalCount = approvalMapper.updateApprovalToHold(approvalId);
        if (updatedApprovalCount != 1) {
            throw new BadRequestException("이미 처리된 문서입니다.");
        }

        Long drafterId = approvalMapper.findDrafterIdByApprovalId(approvalId);
        if (drafterId == null) {
            return;
        }

        EmployeeProfileDTO approverProfile = hrFacade.getEmployeeProfile(approverId);
        EmployeeProfileDTO drafterProfile = hrFacade.getEmployeeProfile(drafterId);
        if (drafterProfile.email() == null || drafterProfile.email().isBlank()) {
            return;
        }

        String title = approvalMapper.findTitleByApprovalId(approvalId);
        String safeTitle = (title == null || title.isBlank()) ? "제목 없음" : title;
        String approverName =
                approverProfile.employeeName() == null ? "결재자" : approverProfile.employeeName();

        safePublishEmailEvent(
                drafterProfile.email(),
                "[RHIGHT] 결재 보류: " + safeTitle,
                "<p>" + approverName + "님이 문서를 보류했습니다.</p><p>문서 제목: " + safeTitle + "</p>");
    }

    private void publishFinalApprovedEvent(Long approvalId) {
        ApprovalHeaderRow header =
                approvalMapper
                        .findApprovalHeaderByApprovalId(approvalId)
                        .orElseThrow(() -> new ApprovalNotFoundException("존재하지 않는 기안입니다."));

        LocalDateTime approvedAt = header.approveDate();
        if (approvedAt == null) {
            throw new IllegalStateException("최종 승인 시 approve_dt가 없습니다. approvalId=" + approvalId);
        }

        switch (header.docType()) {
            case "VACATION" -> {
                VacationDetailRow row =
                        vacationMapper
                                .findVacationDetailByApprovalId(approvalId)
                                .orElseThrow(
                                        () -> new ApprovalNotFoundException("기안 상세를 찾을 수 없습니다."));
                eventPublisher.publishEvent(
                        new ApprovalVacationEvent(
                                approvalId,
                                approvedAt,
                                row.vacationType(),
                                row.startDate(),
                                row.endDate(),
                                row.reason()));
            }
            case "OVERTIME" -> {
                OvertimeDetailRow row =
                        overtimeMapper
                                .findOvertimeDetailByApprovalId(approvalId)
                                .orElseThrow(
                                        () -> new ApprovalNotFoundException("기안 상세를 찾을 수 없습니다."));
                eventPublisher.publishEvent(
                        new ApprovalOvertimeEvent(
                                approvalId,
                                approvedAt,
                                row.workDate(),
                                row.startTime(),
                                row.endTime(),
                                row.reason()));
            }
            case "FLEXIBLE" -> {
                FlexibleWorkDetailRow row =
                        flexibleWorkMapper
                                .findFlexibleWorkDetailByApprovalId(approvalId)
                                .orElseThrow(
                                        () -> new ApprovalNotFoundException("기안 상세를 찾을 수 없습니다."));
                eventPublisher.publishEvent(
                        new ApprovalFlexibleEvent(
                                approvalId,
                                approvedAt,
                                row.startDate(),
                                row.endDate(),
                                row.reason()));
            }
            case "TRIP" -> {
                BusinessTripDetailRow row =
                        businessTripMapper
                                .findBusinessTripDetailByApprovalId(approvalId)
                                .orElseThrow(
                                        () -> new ApprovalNotFoundException("기안 상세를 찾을 수 없습니다."));
                eventPublisher.publishEvent(
                        new ApprovalTripEvent(
                                approvalId,
                                approvedAt,
                                row.tripType(),
                                row.destination(),
                                row.startDate(),
                                row.endDate(),
                                row.reason()));
            }
            case "LEAVE" -> {
                LeaveDetailRow row =
                        leaveMapper
                                .findLeaveDetailByApprovalId(approvalId)
                                .orElseThrow(
                                        () -> new ApprovalNotFoundException("기안 상세를 찾을 수 없습니다."));
                eventPublisher.publishEvent(
                        new ApprovalLeaveEvent(
                                approvalId,
                                approvedAt,
                                row.startDate(),
                                row.endDate(),
                                row.leaveType(),
                                row.reason()));
            }
            case "RTW" -> {
                RTWDetailRow row =
                        rtwMapper
                                .findRTWDetailByApprovalId(approvalId)
                                .orElseThrow(
                                        () -> new ApprovalNotFoundException("기안 상세를 찾을 수 없습니다."));
                eventPublisher.publishEvent(
                        new ApprovalRTWEvent(approvalId, approvedAt, row.rtwDate(), row.reason()));
            }
            default -> throw new ApprovalNotFoundException("지원하지 않는 문서 타입입니다.");
        }
    }

    private void publishMailToEmployeeIds(Set<Long> employeeIds, String subject, String body) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return;
        }

        employeeIds.stream()
                .filter(id -> id != null)
                .forEach(
                        employeeId -> {
                            EmployeeProfileDTO profile = hrFacade.getEmployeeProfile(employeeId);
                            if (profile.email() == null || profile.email().isBlank()) {
                                return;
                            }
                            safePublishEmailEvent(profile.email(), subject, body);
                        });
    }
}
