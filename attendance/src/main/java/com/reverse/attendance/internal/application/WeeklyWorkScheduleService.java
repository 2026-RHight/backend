package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.domain.WeeklyWorkSchedule;
import com.reverse.attendance.internal.domain.enums.ApprovalStatus;
import com.reverse.attendance.internal.dto.request.WeeklyWorkScheduleApplyRequest;
import com.reverse.attendance.internal.dto.request.WeeklyWorkScheduleProcessRequest;
import com.reverse.attendance.internal.persistence.WeeklyWorkScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyWorkScheduleService {

    private final WeeklyWorkScheduleMapper scheduleMapper;

    @Transactional
    public void applySchedule(WeeklyWorkScheduleApplyRequest request, Long employeeId) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("유연근무 종료 시간이 시작 시간보다 빠를 수 없습니다.");
        }

        WeeklyWorkSchedule schedule = WeeklyWorkSchedule.builder()
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
    public List<WeeklyWorkSchedule> getMySchedules(Long employeeId) {
        return scheduleMapper.findByEmployeeId(employeeId);
    }

    @Transactional
    public void cancelSchedule(Long weeklyId, Long employeeId) {
        WeeklyWorkSchedule schedule = scheduleMapper.findById(weeklyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유연근무 신청 내역을 찾을 수 없습니다."));

        if (!schedule.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("본인의 신청 건만 취소할 수 있습니다.");
        }
        if (schedule.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("결재 대기 상태인 건만 취소할 수 있습니다.");
        }

        WeeklyWorkSchedule canceledSchedule = WeeklyWorkSchedule.builder()
                .weeklyId(schedule.getWeeklyId())
                .approvalStatus(ApprovalStatus.CANCELED)
                .build();

        scheduleMapper.updateStatus(canceledSchedule);
    }

    @Transactional(readOnly = true)
    public List<WeeklyWorkSchedule> getAllSchedules(String status) {
        return scheduleMapper.findAll(status);
    }

    @Transactional
    public void processSchedule(WeeklyWorkScheduleProcessRequest request) {
        WeeklyWorkSchedule schedule = scheduleMapper.findById(request.getWeeklyId())
                .orElseThrow(() -> new IllegalArgumentException("결재할 신청 내역을 찾을 수 없습니다."));

        if (schedule.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("대기 상태인 신청 건만 결재할 수 있습니다.");
        }

        ApprovalStatus newStatus = request.isApprove() ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED;

        WeeklyWorkSchedule processedSchedule = WeeklyWorkSchedule.builder()
                .weeklyId(schedule.getWeeklyId())
                .approvalStatus(newStatus)
                .build();

        scheduleMapper.updateStatus(processedSchedule);
    }
}
