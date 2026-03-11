package com.reverse.performance.internal.application;

import com.reverse.core.exception.BadRequestException;
import com.reverse.core.exception.ForbiddenException;
import com.reverse.performance.internal.dto.request.AttachmentRequest;
import com.reverse.performance.internal.dto.request.PerformanceResultUpdateRequest;
import com.reverse.performance.internal.dto.response.PerformanceInquiryItemResponse;
import com.reverse.performance.internal.exception.PerformanceActionNotAllowedException;
import com.reverse.performance.internal.exception.PerformanceNotFoundException;
import com.reverse.performance.internal.persistence.AttachmentMapper;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceInquiryService {

    private final PerformanceHrMemberResolver performanceHrMemberResolver;
    private final PerformanceViewMapper performanceViewMapper;
    private final AttachmentMapper attachmentMapper;
    private final PerformanceFileService performanceFileService;

    public List<PerformanceInquiryItemResponse> getInquiryItems(
            Long viewerEmployeeId, Long targetEmployeeId, boolean isAdmin) {
        List<PerformanceViewMapper.InquiryItemRow> rows =
                resolveInquiryRows(viewerEmployeeId, targetEmployeeId, isAdmin);
        return rows.stream()
                .map(
                        row -> {
                            PerformanceHrMemberResolver.EmployeeProfileSnapshot profile =
                                    performanceHrMemberResolver.getEmployeeProfile(
                                            row.employeeId());
                            return new PerformanceInquiryItemResponse(
                                    row.id(),
                                    row.type(),
                                    row.title(),
                                    row.coreTask(),
                                    row.date(),
                                    row.status(),
                                    row.progress(),
                                    profile == null ? "-" : profile.employeeName(),
                                    row.employeeId(),
                                    row.description(),
                                    row.achievement());
                        })
                .toList();
    }

    private List<PerformanceViewMapper.InquiryItemRow> resolveInquiryRows(
            Long viewerEmployeeId, Long targetEmployeeId, boolean isAdmin) {
        if (targetEmployeeId != null) {
            if (!isAdmin
                    && !targetEmployeeId.equals(viewerEmployeeId)
                    && !canAccessTarget(viewerEmployeeId, targetEmployeeId)) {
                throw new ForbiddenException("FORBIDDEN", "조회할 수 없는 팀원 성과입니다.");
            }
            return performanceViewMapper.findInquiryItems(
                    viewerEmployeeId,
                    targetEmployeeId,
                    isAdmin || !targetEmployeeId.equals(viewerEmployeeId));
        }

        if (isAdmin) {
            return performanceViewMapper.findInquiryItems(viewerEmployeeId, null, true);
        }

        List<Long> targetIds =
                performanceViewMapper.findInquiryAccessibleTargetIds(viewerEmployeeId);
        if (targetIds == null || targetIds.isEmpty()) {
            return performanceViewMapper.findInquiryItems(
                    viewerEmployeeId, viewerEmployeeId, false);
        }
        return performanceViewMapper.findInquiryItemsByEmployeeIds(targetIds);
    }

    @Transactional
    public void updateResult(
            Long callerEmployeeId, Long performanceId, PerformanceResultUpdateRequest request) {
        updateResult(callerEmployeeId, performanceId, request, List.of());
    }

    @Transactional
    public void updateResult(
            Long callerEmployeeId,
            Long performanceId,
            PerformanceResultUpdateRequest request,
            List<MultipartFile> files) {
        if (request == null) {
            throw new BadRequestException("성과 결과 등록 요청이 비어 있습니다.");
        }
        validateUpdatePermission(callerEmployeeId, performanceId);

        String summary = blankToNull(request.resultSummary());
        int performanceUpdated =
                performanceViewMapper.updatePerformanceResult(
                        performanceId, request.progress(), summary);
        if (performanceUpdated == 0) {
            throw new PerformanceNotFoundException("결과를 등록할 성과를 찾을 수 없습니다.");
        }

        int personalUpdated =
                performanceViewMapper.updatePersonalResult(
                        performanceId,
                        summary,
                        blankToNull(request.growthPoint()),
                        blankToNull(request.improvementPoint()));
        int teamUpdated =
                performanceViewMapper.updateTeamResult(
                        performanceId, summary, blankToNull(request.resultNote()));
        if (personalUpdated == 0 && teamUpdated == 0) {
            throw new PerformanceActionNotAllowedException("성과 상세 정보가 없어 결과를 저장할 수 없습니다.");
        }

        saveAttachments(performanceId, files);
    }

    private void validateUpdatePermission(Long callerEmployeeId, Long performanceId) {
        if (callerEmployeeId == null) {
            throw new ForbiddenException("FORBIDDEN", "성과 결과를 수정할 권한이 없습니다.");
        }
        int authorized =
                performanceViewMapper.countOwnedPerformance(callerEmployeeId, performanceId);
        if (authorized == 0) {
            throw new ForbiddenException("FORBIDDEN", "성과 결과를 수정할 권한이 없습니다.");
        }
    }

    private boolean canAccessTarget(Long viewerEmployeeId, Long targetEmployeeId) {
        Integer accessible =
                performanceViewMapper.countInquiryAccessibleTarget(
                        viewerEmployeeId, targetEmployeeId);
        return accessible != null && accessible > 0;
    }

    private void saveAttachments(Long performanceId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        List<String> uploadedKeys = new ArrayList<>();
        registerRollbackDelete(uploadedKeys);

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            PerformanceFileService.UploadResult uploaded =
                    performanceFileService.upload(file, "performance/result/" + performanceId);
            uploadedKeys.add(uploaded.key());

            attachmentMapper.saveAttachment(
                    new AttachmentRequest(
                            null,
                            performanceId,
                            uploaded.originalName(),
                            uploaded.fileUrl(),
                            null,
                            LocalDateTime.now()));
        }
    }

    private void registerRollbackDelete(List<String> uploadedKeys) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            uploadedKeys.forEach(PerformanceInquiryService.this::deleteQuietly);
                        }
                    }
                });
    }

    private void deleteQuietly(String key) {
        try {
            performanceFileService.delete(key);
        } catch (RuntimeException ignored) {
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
