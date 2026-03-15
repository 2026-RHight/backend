package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.domain.WeeklyWorkSchedule;
import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import com.reverse.attendance.internal.dto.request.WeeklyWorkScheduleApplyRequest;
import com.reverse.attendance.internal.dto.request.WeeklyWorkScheduleProcessRequest;
import com.reverse.attendance.internal.dto.response.TeamWeeklyScheduleDayResponse;
import com.reverse.attendance.internal.dto.response.TeamWeeklyScheduleEntryResponse;
import com.reverse.attendance.internal.dto.response.TeamWeeklyScheduleOverviewResponse;
import com.reverse.attendance.internal.dto.response.WeeklyWorkScheduleResponse;
import com.reverse.attendance.internal.persistence.WeeklyWorkScheduleMapper;
import com.reverse.core.response.PageResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeeklyWorkScheduleService {

    private final WeeklyWorkScheduleMapper scheduleMapper;
    private final AttendanceSyncService attendanceSyncService;

    @Transactional
    public void applySchedule(WeeklyWorkScheduleApplyRequest request, Long employeeId) {
        applySchedule(request, employeeId, null);
    }

    @Transactional
    public void applySchedule(
            WeeklyWorkScheduleApplyRequest request, Long employeeId, Long approvalId) {
        if (request == null
                || request.getStartDate() == null
                || request.getEndDate() == null
                || request.getPlanDate() == null) {
            throw new IllegalArgumentException("시작/종료 시간과 근무일은 필수입니다.");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("유연근무 종료 시간이 시작 시간보다 빠를 수 없습니다.");
        }

        // 동시성(중복 신청) 방지를 위해 직원 기준으로 DB 락 획득
        scheduleMapper.lockEmployee(employeeId);

        int overlapCount =
                scheduleMapper.countOverlappingSchedules(
                        employeeId,
                        request.getPlanDate(),
                        request.getStartDate(),
                        request.getEndDate());
        if (overlapCount > 0) {
            throw new com.reverse.core.exception.BadRequestException(
                    "해당 기간에 이미 신청했거나 승인된 유연근무가 존재합니다.");
        }

        WeeklyWorkSchedule schedule =
                WeeklyWorkSchedule.builder()
                        .approvalId(approvalId)
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

    @Transactional
    public void deleteLinkedRequestByApprovalId(Long approvalId) {
        if (approvalId == null) {
            return;
        }
        scheduleMapper.deleteByApprovalId(approvalId);
    }

    @Transactional(readOnly = true)
    public PageResponse<WeeklyWorkScheduleResponse> getMySchedules(
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
        return PageResponse.of(
                content.stream().map(WeeklyWorkScheduleResponse::from).collect(Collectors.toList()),
                page,
                size,
                totalElements);
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
                                () ->
                                        new com.reverse.core.exception.NotFoundException(
                                                "해당 유연근무 신청 내역을 찾을 수 없습니다."));

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
    public PageResponse<WeeklyWorkScheduleResponse> getTeamSchedules(
            Long actorEmployeeId, String status, int page, int size) {
        if (status != null) {
            status = status.trim();
            if (status.isEmpty()) {
                status = null;
            } else {
                try {
                    status = ApprovalStatus.valueOf(status).name();
                } catch (IllegalArgumentException e) {
                    throw new com.reverse.core.exception.BadRequestException("유효하지 않은 결재 상태입니다.");
                }
            }
        }
        page = Math.max(1, page);
        size = Math.min(100, Math.max(1, size));
        int limit = size;
        long offsetLong = (long) (page - 1) * size;
        if (offsetLong > Integer.MAX_VALUE) {
            throw new com.reverse.core.exception.BadRequestException("조회 가능한 페이지 범위를 초과했습니다.");
        }
        int offset = (int) offsetLong;
        List<WeeklyWorkSchedule> content =
                scheduleMapper.findTeamSchedules(actorEmployeeId, status, limit, offset);
        long totalElements = scheduleMapper.countTeamSchedules(actorEmployeeId, status);
        return PageResponse.of(
                content.stream().map(WeeklyWorkScheduleResponse::from).collect(Collectors.toList()),
                page,
                size,
                totalElements);
    }

    @Transactional
    public void processSchedule(WeeklyWorkScheduleProcessRequest request, Long actorEmployeeId) {
        WeeklyWorkSchedule schedule =
                scheduleMapper
                        .findById(request.getWeeklyId())
                        .orElseThrow(
                                () ->
                                        new com.reverse.core.exception.NotFoundException(
                                                "결재할 신청 내역을 찾을 수 없습니다."));

        if (!scheduleMapper.isSameTeamSchedule(actorEmployeeId, request.getWeeklyId())) {
            throw new com.reverse.core.exception.ForbiddenException(
                    "같은 부서 팀원의 유연근무 신청만 처리할 수 있습니다.");
        }

        if (schedule.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new com.reverse.core.exception.BadRequestException("대기 상태인 신청 건만 결재할 수 있습니다.");
        }

        ApprovalStatus newStatus =
                request.isApprove() ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED;

        if (!request.isApprove()
                && (request.getRejectReason() == null
                        || request.getRejectReason().trim().isEmpty())) {
            throw new com.reverse.core.exception.BadRequestException("반려 시 사유를 반드시 입력해야 합니다.");
        }

        WeeklyWorkSchedule processedSchedule =
                WeeklyWorkSchedule.builder()
                        .weeklyId(schedule.getWeeklyId())
                        .approvalStatus(newStatus)
                        .rejectReason(request.isApprove() ? null : request.getRejectReason().trim())
                        .build();

        int updatedRows = scheduleMapper.updateStatusIfPending(processedSchedule);
        if (updatedRows == 0) {
            throw new com.reverse.core.exception.BadRequestException("이미 처리된 신청 건입니다.");
        }
        if (request.isApprove()) {
            attendanceSyncService.recordApprovedWeeklySchedule(schedule);
        }
    }

    @Transactional(readOnly = true)
    public TeamWeeklyScheduleOverviewResponse getTeamWeeklyOverview(
            Long actorEmployeeId, LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        LocalDate weekStart = targetDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(4);

        Map<LocalDate, List<WeeklyWorkSchedule>> schedulesByDate =
                scheduleMapper
                        .findTeamSchedulesByEmployeeIdAndPlanDateRange(
                                actorEmployeeId, weekStart, weekEnd)
                        .stream()
                        .filter(schedule -> schedule.getApprovalStatus() != ApprovalStatus.CANCELED)
                        .collect(Collectors.groupingBy(WeeklyWorkSchedule::getPlanDate));

        List<TeamWeeklyScheduleDayResponse> days =
                Stream.iterate(
                                weekStart,
                                current -> !current.isAfter(weekEnd),
                                current -> current.plusDays(1))
                        .map(
                                planDate -> {
                                    List<WeeklyWorkSchedule> entries =
                                            schedulesByDate.getOrDefault(planDate, List.of());
                                    int requestedEmployeeCount =
                                            (int)
                                                    entries.stream()
                                                            .filter(
                                                                    schedule ->
                                                                            schedule
                                                                                                    .getApprovalStatus()
                                                                                            == ApprovalStatus
                                                                                                    .PENDING
                                                                                    || schedule
                                                                                                    .getApprovalStatus()
                                                                                            == ApprovalStatus
                                                                                                    .APPROVED)
                                                            .count();
                                    int approvedEmployeeCount =
                                            (int)
                                                    entries.stream()
                                                            .filter(
                                                                    schedule ->
                                                                            schedule
                                                                                            .getApprovalStatus()
                                                                                    == ApprovalStatus
                                                                                            .APPROVED)
                                                            .count();
                                    int coreCoverageCount =
                                            (int)
                                                    entries.stream()
                                                            .filter(
                                                                    schedule ->
                                                                            schedule
                                                                                                    .getApprovalStatus()
                                                                                            == ApprovalStatus
                                                                                                    .PENDING
                                                                                    || schedule
                                                                                                    .getApprovalStatus()
                                                                                            == ApprovalStatus
                                                                                                    .APPROVED)
                                                            .filter(
                                                                    schedule ->
                                                                            !schedule.getStartDate()
                                                                                            .toLocalTime()
                                                                                            .isAfter(
                                                                                                    java
                                                                                                            .time
                                                                                                            .LocalTime
                                                                                                            .of(
                                                                                                                    14,
                                                                                                                    0))
                                                                                    && !schedule.getEndDate()
                                                                                            .toLocalTime()
                                                                                            .isBefore(
                                                                                                    java
                                                                                                            .time
                                                                                                            .LocalTime
                                                                                                            .of(
                                                                                                                    16,
                                                                                                                    0)))
                                                            .count();

                                    return TeamWeeklyScheduleDayResponse.builder()
                                            .planDate(planDate)
                                            .requestedEmployeeCount(requestedEmployeeCount)
                                            .approvedEmployeeCount(approvedEmployeeCount)
                                            .coreTimeShortageRisk(coreCoverageCount < 2)
                                            .entries(
                                                    entries.stream()
                                                            .map(
                                                                    schedule ->
                                                                            TeamWeeklyScheduleEntryResponse
                                                                                    .builder()
                                                                                    .weeklyId(
                                                                                            schedule
                                                                                                    .getWeeklyId())
                                                                                    .employeeId(
                                                                                            schedule
                                                                                                    .getEmployeeId())
                                                                                    .employeeName(
                                                                                            schedule
                                                                                                    .getEmployeeName())
                                                                                    .departmentName(
                                                                                            schedule
                                                                                                    .getDepartmentName())
                                                                                    .positionName(
                                                                                            schedule
                                                                                                    .getPositionName())
                                                                                    .planDate(
                                                                                            schedule
                                                                                                    .getPlanDate())
                                                                                    .startDate(
                                                                                            schedule
                                                                                                    .getStartDate())
                                                                                    .endDate(
                                                                                            schedule
                                                                                                    .getEndDate())
                                                                                    .workForm(
                                                                                            schedule
                                                                                                    .getWorkForm())
                                                                                    .scheduleTitle(
                                                                                            schedule
                                                                                                    .getScheduleTitle())
                                                                                    .memo(
                                                                                            schedule
                                                                                                    .getMemo())
                                                                                    .rejectReason(
                                                                                            schedule
                                                                                                    .getRejectReason())
                                                                                    .approvalStatus(
                                                                                            schedule
                                                                                                    .getApprovalStatus())
                                                                                    .build())
                                                            .collect(Collectors.toList()))
                                            .build();
                                })
                        .collect(Collectors.toList());

        return TeamWeeklyScheduleOverviewResponse.builder()
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .days(days)
                .build();
    }
}
