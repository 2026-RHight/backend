package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.request.PerformanceResultUpdateRequest;
import com.reverse.performance.internal.dto.request.AttachmentRequest;
import com.reverse.performance.internal.dto.response.PerformanceInquiryItemResponse;
import com.reverse.performance.internal.exception.PerformanceActionNotAllowedException;
import com.reverse.performance.internal.exception.PerformanceNotFoundException;
import com.reverse.performance.internal.persistence.AttachmentMapper;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceInquiryService {

    private final PerformanceViewMapper performanceViewMapper;
    private final AttachmentMapper attachmentMapper;
    private final PerformanceFileService performanceFileService;

    public List<PerformanceInquiryItemResponse> getInquiryItems(
            Long viewerEmployeeId, Long targetEmployeeId, boolean isAdmin) {
        return performanceViewMapper.findInquiryItems(viewerEmployeeId, targetEmployeeId, isAdmin);
    }

    @Transactional
    public void updateResult(Long performanceId, PerformanceResultUpdateRequest request) {
        updateResult(performanceId, request, List.of());
    }

    @Transactional
    public void updateResult(
            Long performanceId,
            PerformanceResultUpdateRequest request,
            List<MultipartFile> files) {
        String summary = blankToNull(request.resultSummary());
        int performanceUpdated =
                performanceViewMapper.updatePerformanceResult(performanceId, request.progress(), summary);
        if (performanceUpdated == 0) {
            throw new PerformanceNotFoundException("결과를 등록할 성과를 찾을 수 없습니다.");
        }

        int personalUpdated = performanceViewMapper.updatePersonalResult(
                performanceId,
                summary,
                blankToNull(request.growthPoint()),
                blankToNull(request.improvementPoint())
        );
        int teamUpdated = performanceViewMapper.updateTeamResult(
                performanceId,
                summary,
                blankToNull(request.resultNote())
        );
        if (personalUpdated == 0 && teamUpdated == 0) {
            throw new PerformanceActionNotAllowedException("성과 상세 정보가 없어 결과를 저장할 수 없습니다.");
        }

        saveAttachments(performanceId, files);
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
                            LocalDateTime.now()
                    )
            );
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
