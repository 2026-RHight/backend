package com.reverse.approval.internal.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record ApprovalDetailResponse(
        Long approvalId,
        String docId,
        String docType,
        String title,
        String approvalStatus,
        LocalDateTime draftDate,
        LocalDateTime approveDate,
        Long drafterId,
        String drafterName,
        String departmentName,
        List<ApprovalLineItem> approvalLines,
        List<ReferenceLineItem> referenceLines,
        List<RecipientLineItem> recipientLines,
        List<AttachmentItem> attachments,
        VacationDetail vacationDetail,
        OvertimeDetail overtimeDetail,
        FlexibleWorkDetail flexibleWorkDetail,
        BusinessTripDetail businessTripDetail,
        LeaveDetail leaveDetail,
        RTWDetail rtwDetail) {
    public record ApprovalLineItem(
            Byte approvalSeq,
            String approvalStatus,
            Long approverId,
            String approverName,
            String approverRank,
            String reason,
            LocalDateTime approvedDate,
            LocalDateTime readDate) {}

    public record ReferenceLineItem(
            Long referencerId,
            String referencerName,
            String referenceRank,
            LocalDateTime readDate) {}

    public record RecipientLineItem(
            Long receiverId, String receiverName, String receiverRank, LocalDateTime readDate) {}

    public record AttachmentItem(
            Long fileId, String filePath, String originalName, LocalDateTime createdDate) {}

    public record VacationDetail(
            String vacationType, LocalDateTime startDate, LocalDateTime endDate, String reason) {}

    public record OvertimeDetail(
            LocalDate workDate, LocalTime startTime, LocalTime endTime, String reason) {}

    public record FlexibleWorkDetail(
            LocalDateTime startDate, LocalDateTime endDate, String reason) {}

    public record BusinessTripDetail(
            String tripType,
            String destination,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String reason) {}

    public record LeaveDetail(
            LocalDateTime startDate, LocalDateTime endDate, String leaveType, String reason) {}

    public record RTWDetail(LocalDate rtwDate, String reason) {}
}
