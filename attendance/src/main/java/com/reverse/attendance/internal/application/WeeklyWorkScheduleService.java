package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.domain.WeeklyWorkSchedule;
import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import com.reverse.attendance.internal.dto.request.WeeklyWorkScheduleApplyRequest;
import com.reverse.attendance.internal.dto.request.WeeklyWorkScheduleProcessRequest;
import com.reverse.attendance.internal.persistence.WeeklyWorkScheduleMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeeklyWorkScheduleService {

    private final WeeklyWorkScheduleMapper scheduleMapper;

    @Transactional
    public void applySchedule(WeeklyWorkScheduleApplyRequest request, Long employeeId) {
        if (request == null
                || request.getStartDate() == null
                || request.getEndDate() == null
                || request.getPlanDate() == null) {
            throw new IllegalArgumentException("시작/종료 시간과 근무일은 필수입니다.");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("유연근무 종료 시간이 시작 시간보다 빠를 수 없습니다.");
        }

        int overlapCount =
                scheduleMapper.countOverlappingSchedules(
                        employeeId, request.getStartDate(), request.getEndDate());
        if (overlapCount > 0) {
            throw new com.reverse.core.exception.BadRequestException(
                    "해당 기간에 이미 신청했거나 승인된 유연근무가 존재합니다.");
        }

        WeeklyWorkSchedule schedule =
                WeeklyWorkSchedule.builder()
                        .employeeId(employeeId)
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .planDate(request.getPlanDate())
                        .workForm(request.getWorkForm())
                        .scheduleTitle(request.getScheduleTitle())
                        .memo(request.getMemo())
                        .approvalStatus(ApprovalStatus.PENDING)
                        .build();

        scheduleMapper.insertSchedule(schedule);
    }

    @Transactional(readOnly = true)
    public com.reverse.core.response.PageResponse<WeeklyWorkSchedule> getMySchedules(
            Long employeeId, int page, int size) {
        page = Math.max(1, page);
        size = Math.min(100, Math.max(1, size));
        int limit = size;
        long offsetLong = (long) (page - 1) * size;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new com.reverse.core.exception.BadRequestException("조회 가능한 페이지 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;
        List<WeeklyWorkSchedule> content =
                scheduleMapper.findByEmployeeId(employeeId, limit, offset);
        long totalElements = scheduleMapper.countByEmployeeId(employeeId);
        return com.reverse.core.response.PageResponse.of(content, page, size, totalElements);
    }

    @Transactional(readOnly = true)
    public com.reverse.attendance.internal.dto.response.RequestStatusCountResponse
            getMyRequestStatusCounts(Long employeeId) {
        return scheduleMapper.countRequestStatus(employeeId);
    }

    @Transactional
    public void cancelSchedule(Long weeklyId, Long employeeId) {
        WeeklyWorkSchedule schedule =
                scheduleMapper
                        .findById(weeklyId)
                        .orElseThrow(
                                () -> new IllegalArgumentException("해당 유연근무 신청 내역을 찾을 수 없습니다."));

        if (!schedule.getEmployeeId().equals(employeeId)) {
            throw new com.reverse.core.exception.ForbiddenException("본인의 신청 건만 취소할 수 있습니다.");
        }
        if (schedule.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new com.reverse.core.exception.BadRequestException("결재 대기 상태인 건만 취소할 수 있습니다.");
        }

        WeeklyWorkSchedule canceledSchedule =
                WeeklyWorkSchedule.builder()
                        .weeklyId(schedule.getWeeklyId())
                        .approvalStatus(ApprovalStatus.CANCELED)
                        .build();

        int updatedRows = scheduleMapper.updateStatusIfPending(canceledSchedule);
        if (updatedRows == 0) {
            throw new com.reverse.core.exception.BadRequestException("이미 처리된 신청 건입니다.");
        }
    }

    @Transactional(readOnly = true)
    public com.reverse.core.response.PageResponse<WeeklyWorkSchedule> getAllSchedules(
            String status, int page, int size) {
        page = Math.max(1, page);
        size = Math.min(100, Math.max(1, size));
        int limit = size;
        long offsetLong = (long) (page - 1) * size;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new com.reverse.core.exception.BadRequestException("조회 가능한 페이지 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;
        List<WeeklyWorkSchedule> content = scheduleMapper.findAll(status, limit, offset);
        long totalElements = scheduleMapper.countAll(status);
        return com.reverse.core.response.PageResponse.of(content, page, size, totalElements);
    }

    @Transactional
    public void processSchedule(WeeklyWorkScheduleProcessRequest request) {
        WeeklyWorkSchedule schedule =
                scheduleMapper
                        .findById(request.getWeeklyId())
                        .orElseThrow(() -> new IllegalArgumentException("결재할 신청 내역을 찾을 수 없습니다."));

        if (schedule.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new com.reverse.core.exception.BadRequestException("대기 상태인 신청 건만 결재할 수 있습니다.");
        }

        ApprovalStatus newStatus =
                request.isApprove() ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED;

        WeeklyWorkSchedule processedSchedule =
                WeeklyWorkSchedule.builder()
                        .weeklyId(schedule.getWeeklyId())
                        .approvalStatus(newStatus)
                        .build();

        int updatedRows = scheduleMapper.updateStatusIfPending(processedSchedule);
        if (updatedRows == 0) {
            throw new com.reverse.core.exception.BadRequestException("이미 처리된 신청 건입니다.");
        }
    }
}
